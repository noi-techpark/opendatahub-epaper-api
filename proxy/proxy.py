from flask import Flask
from flask import request
from flask import jsonify
import requests
from socket import *
import socket
from threading import Thread
from time import sleep
from coolname import generate_slug
from requests.exceptions import ConnectionError



API_URL = "https://api.epaper.opendatahub.testingmachine.eu"
DISPLAY_CREATE_URL =  API_URL + "/display/auto-create/"
LOCAL_TUNNEL_REGISTER_URL =  API_URL + "/display/proxy-register/"
#DISPLAY_CREATE_URL = "https://weak-fireant-56.loca.lt/display/auto-create/"

connexion = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

display_ip_mac_list = []

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
            if not data in display_ip_mac_list: #check if full mac address arrived and not already present in list
                name = ""
                data = data.decode('utf-8')
                display_ip_mac_list.append(data)
                print("messages : ",addr , data)

                URL = "http://" + str(data)

                print(URL)

                #TODO create threads to be non blocking

                #ask state of display
                print("UDP get state")
                response = requests.get(URL, data = "3", timeout=None)
                json = response.json()
                print(json)

                width = json["width"]
                height = json["height"]

                # generates random display name or use predefined one
                if len(json["displayName"]) > 0:
                    name = json["displayName"]
                else:
                    name = generate_slug(3)
                print(name)



                #create-display
                print("UDP autiocreate")
                res = requests.post(DISPLAY_CREATE_URL, data = {"ip" : addr[0], "name" : name, "width" :width, "height" : height, "mac" : json["mac"]}, timeout=None)
                print(res)

                #remove from list to be bale to reconnecnt again
                display_ip_mac_list.remove(data)


@app.route('/test', methods=['GET'])
def test():
	print ("Proxy Connection Test OK")
	return "True\n"

@app.route('/send', methods=['POST'])
def send():
    req_data = request.get_json()
    print(request.args['ip'])
    URL = "http://" + request.args['ip']
    try:
        print("SEND to " + URL)
        response = requests.get(url = URL, data = req_data["image"], timeout=None)
        return jsonify(response.json())
    except ConnectionError:
        print("ConnectionError")
        return {"errorMessage" : "ConnectionError"}

@app.route('/clear', methods=['POST'])
def clear():
    print(request.args['ip'])
    URL = "http://" + request.args['ip']
    print("CLEAR of: " + URL)
    try:
        response = requests.get(url = URL, data = "2", timeout=None) # 2 as data means clear
        return jsonify(response.json())
    except ConnectionError:
        print("ConnectionError")
        return {"errorMessage" : "ConnectionError"}

@app.route('/state', methods=['POST'])
def state():
    URL = "http://" + request.args['ip']
    print("STATE of: " + URL)
    try:
        response = requests.get(url = URL, data = "3", timeout=None) # 3 as data means get state
        return jsonify(response.json())
    except ConnectionError:
        print("ConnectionError")
        return {"errorMessage" : "ConnectionError"}


if __name__ == '__main__':

    #read localtunnel URL from log file and post to API
    local_tunnel_url = open('local-tunnel.log', 'r').read()
    print(local_tunnel_url)
    local_tunnel_url = local_tunnel_url.replace("your url is: ", "").replace("\n","")
    print(local_tunnel_url)
    res = requests.post(DISPLAY_CREATE_URL, data = {"url" : local_tunnel_url})
    print(res)

    #start proxy
    thread = Thread(target = threaded_function, args = (10, ))
    thread.start()
    app.run(debug=False, host='0.0.0.0', port=5000)


