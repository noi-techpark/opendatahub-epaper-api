import websocket
from threading import Thread
import time
import stomper as stomper



def on_message(ws, message):
    print("### message ###")
    print(message)

def on_error(ws, error):
    print("### error ###")
    print(error)

def on_close(ws):
    print("### closed ###")

def on_open(ws):
    def run(*args):
        ws.send("CONNECT\naccept-version:1.0,1.1,2.0\n\n\x00\n")

        for i in range(3):
            # send the message, then wait
            # so thread doesn't exit and socket
            # isn't closed
            ws.send(stomper.send("/state","Hello %d" % i))
            time.sleep(1)

        time.sleep(1)
        # ws.close()
        # print("Thread terminating...")
        
        sub = stomper.subscribe("/topic/send-image", "proxy", ack='auto')
        ws.send(sub)


    Thread(target=run).start()


if __name__ == "__main__":
    websocket.enableTrace(True)
    ws = websocket.WebSocketApp("ws://localhost:8081/ws",
                                on_message = on_message,
                                on_error = on_error,
                                on_close = on_close)
    ws.on_open = on_open

    ws.run_forever()