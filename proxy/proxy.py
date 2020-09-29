from flask import Flask
from flask import request
import requests
from socket import *
import socket
from threading import Thread
from time import sleep
from coolname import generate_slug
from requests.exceptions import ConnectionError


PROXY_ADDRESS = "localhost:8080"

DISPLAY_CREATE_URL = "http://" + PROXY_ADDRESS + "/display/auto-create/"

connexion = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

display_ip_mac_list = {}

app = Flask(__name__)


def threaded_function(arg):
    with app.test_request_context(): #to be in flask request context
        import requests
        try:
            connexion.bind(('', 5006))
        except socket.error:
            print("connexion failed")
            connexion.close()
            sys.exit()
        print("udp ready")
        while 1:
            data, addr = connexion.recvfrom(120)
            if len(data) == 17 and not data in display_ip_mac_list: #check if full mac address arrived and not already present in list
                name = ""
                display_ip_mac_list[data] = addr[0]
                print("messages : ",addr , data)
               
                URL = "http://" + str(addr[0])

                print(URL)  

                #TODO create threads to be non blocking

                #ask state of display
                response = requests.get(URL, data = "3")
                json = response.json()
                print(json)

                width = json["width"]
                height = json["height"]

                # generates random display name or use predefined one
                if len(json["display_name"]) > 0:
                    name = json["display_name"]
                else:
                    name = generate_slug(3)
                print(name)
               
                

                #create-display
                res = requests.post(DISPLAY_CREATE_URL, data = {"ip" : addr[0], "name" : name, "width" :width, "height" : height, "mac" : data})
                print(res)

                #remove from list to be bale to reconnecnt again
                display_ip_mac_list.pop(data)

                


@app.route('/send', methods=['POST'])
def send():
    req_data = request.get_json()
    print(request.args['ip'])
    URL = "http://" + request.args['ip']
    try:
        response = requests.post(url = URL, data = req_data["image"], timeout=None)
        return response.json()
    except ConnectionError:
        print("ConnectionError")
        return {"errorMessage" : "ConnectionError"}

@app.route('/clear', methods=['POST'])
def clear():
    print(request.args['ip'])
    URL = "http://" + request.args['ip']
    try:
        response = requests.get(url = URL, data = "2", timeout=None) # 2 as data means clear 
        return response.json()
    except ConnectionError:
        print("ConnectionError")
        return {"errorMessage" : "ConnectionError"}

@app.route('/state', methods=['POST'])
def state():
    print(request.args['ip'])
    URL = "http://" + request.args['ip']
    try:
        response = requests.get(url = URL, data = "3", timeout=None) # 3 as data means get state
        return response.json()
    except ConnectionError:
        print("ConnectionError")
        return {"errorMessage" : "ConnectionError"}


if __name__ == '__main__':
    thread = Thread(target = threaded_function, args = (10, ))
    thread.start()
    app.run()


