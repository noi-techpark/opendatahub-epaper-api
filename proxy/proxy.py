from flask import Flask
from flask import request
import requests

app = Flask(__name__)


@app.route('/')
def hello():
    return "Hello World!"

@app.route('/send', methods=['POST'])
def send():
    print(request.args['ip'])
    URL = "http://" + request.args['ip']
    r = requests.get(url = URL, data = request.args['image'])
    return "Done"

@app.route('/clear', methods=['POST'])
def clear():
    print(request.args['ip'])
    URL = "http://" + request.args['ip']
    r = requests.get(url = URL, data = "2") # 2 as data means clear 
    return "Done"

@app.route('/state', methods=['POST'])
def state():
    print(request.args['ip'])
    URL = "http://" + request.args['ip']
    response = requests.get(url = URL, data = "3") # 3 as data means get state
    print(response.json())
    return "1;1;100;192.168.1.10;plapal"

if __name__ == '__main__':
    app.run()