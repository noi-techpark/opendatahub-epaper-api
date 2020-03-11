from flask import Flask
from flask import request
import requests

app = Flask(__name__)


@app.route('/send', methods=['POST'])
def send():
    req_data = request.get_json()
    print(request.args['ip'])

    URL = "http://" + request.args['ip']
    response = requests.get(url = URL, data = req_data["image"])
    return response.content

@app.route('/clear', methods=['POST'])
def clear():
    print(request.args['ip'])
    URL = "http://" + request.args['ip']
    response = requests.get(url = URL, data = "2") # 2 as data means clear 
    return response.content

@app.route('/state', methods=['POST'])
def state():
    print(request.args['ip'])
    URL = "http://" + request.args['ip']
    response = requests.get(url = URL, data = "3") # 3 as data means get state
    return response.content

if __name__ == '__main__':
    app.run()