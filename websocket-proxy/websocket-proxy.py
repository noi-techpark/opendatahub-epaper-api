# uses WebSockets for communiaction bewtween API and proxy AND http between proxy and displays


import websocket
from threading import Thread
import time
import stomper as stomper
import json
import requests
from flask import jsonify
from decouple import config
import socket
from coolname import generate_slug

WS_URL = config('WS_URL')
API_URL = config('API_URL')

print(f"API_URL = {API_URL}")
print(f"WS_URL = {WS_URL}")

DISPLAY_CREATE_URL =  API_URL + "/display/auto-create/"

connexion = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

display_ip_mac_list = []

def is_json(myjson):
  try:
    json.loads(myjson)
  except ValueError: # as e:
    # print(e)
    return False
  return True

def on_message(ws, message):
    print("### message ###")
    msg = {}
    dest = "no dest"
    #convert message string to json by ignoring header lines
    for line in message.splitlines():

        #get destination mapping
        if "destination" in line:
            dest = line.split(":")[1]

        maybe_json = line[:-1] # remove last char of string to be valid JSON
        if is_json(maybe_json):
            msg = json.loads(maybe_json)
        else:
            print(f"non json line received: {line}")

    # if msg[""]
    # send image

    print("dest: " + dest)


    if "ip" in msg: # checks if ip exists in dict
        print(msg['ip'])
        URL = "http://" + msg["ip"]

        if dest == "/topic/send-image":
            try:
                print("SEND IMAGE to " + URL)

                #split image in 2 parts and send separatly
                image = str(msg["image"])

                firstpart = image[0:61440]
                secondpart = image[61440:122880]
                thirdpart = image[122880:122880 + 61440]
                fourthpart = image[122880 + 61440:245760]

                print("sending first part")
                response = requests.get(url = URL, data = firstpart, timeout=None)
                print(response)

                print("sending second part")
                response = requests.get(url = URL, data = secondpart, timeout=None)
                print(response)

                print("sending third part")
                response = requests.get(url = URL, data = thirdpart, timeout=None)
                print(response)

                print("sending fourth part")
                response = requests.get(url = URL, data = fourthpart, timeout=None)
                print(response)



                state_dto = response.json()
            except ConnectionError:
                print("ConnectionError")
                state_dto = {"errorMessage" : "ConnectionError"}
        elif dest == "/topic/clear":
            try:
                print("SEND CLEAR to " + URL)
                response = requests.get(url = URL, data = "2", timeout=None) # 2 means clear
                state_dto = jsonify(response.json())
            except ConnectionError:
                print("ConnectionError")
                state_dto = {"errorMessage" : "ConnectionError"}
        elif dest == "/topic/state":
            try:
                print("SEND STATE to " + URL)
                response = requests.get(url = URL, data = "3", timeout=None) # 2 means clear
                state_dto = jsonify(response.json())
            except ConnectionError:
                print("ConnectionError")
                state_dto = {"errorMessage" : "ConnectionError"}

        print(state_dto)
        ws.send(stomper.send("/app/state",state_dto))

def on_error(ws, error):
    print("### error ###")
    print(error)

def on_close(wss):
    print("### closed ###")


def on_open(ws):

    def run(*args):
        print("### open wss ###")
        ws.send("CONNECT\naccept-version:1.0,1.1,2.0\n\n\x00\n")

        time.sleep(1)
        # ws.close()
        # print("Thread terminating...")

        sub = stomper.subscribe("/topic/send-image", "proxy", ack='auto')
        ws.send(sub)
        sub = stomper.subscribe("/topic/clear", "proxy", ack='auto')
        ws.send(sub)
        sub = stomper.subscribe("/topic/state", "proxy", ack='auto')
        ws.send(sub)


    Thread(target=run).start()

# UDP autoreconnect
def udp_autoconnect(arg):
    # with app.test_request_context(): #to be in flask request context
    # import requests
    try:
        connexion.bind(('', 5006))
    except socket.error:
        print("connection failed")
        connexion.close()
        exit()
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

            width = json["w"]
            height = json["h"]

            # generates random display name or use predefined one
            if len(json["d"]) > 0:
                name = json["d"]
            else:
                name = generate_slug(3)
            print(name)

            #create-display
            
            print("UDP autocreate")
            res = requests.post(DISPLAY_CREATE_URL, data = {"ip" : addr[0], "name" : name, "width" :width, "height" : height, "mac" : json["m"]}, timeout=None)
            print(res)

            #remove from list to be bale to reconnecnt again
            display_ip_mac_list.remove(data)

if __name__ == "__main__":
    #Start UDP autoreconnect
    thread = Thread(target = udp_autoconnect, args = (10, ))
    thread.start()

    websocket.enableTrace(True)



    # ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    ws = websocket.WebSocketApp(WS_URL,
                                on_message = on_message,
                                on_error = on_error,
                                on_close = on_close)

    ws.on_open = on_open



    ws.run_forever()
    # to reconnect, but causes stack overflow

    # ws.on_open = on_open
    # while True:
    #      # websocket.enableTrace(True)
    #     try:
    #         ws = websocket.WebSocketApp("ws://localhost:8081/ws",
    #                             on_message = on_message,
    #                             on_error = on_error,
    #                             on_close = on_close)
    #         ws.on_open = on_open
    #         ws.run_forever()
    #     except:
    #         print("Reconnect to API")
    #         time.sleep(4)

