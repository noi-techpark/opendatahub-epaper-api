FROM python:3.8-alpine

RUN mkdir -p /code
WORKDIR /code

COPY proxy/requirements.txt .
COPY proxy/proxy.py .

COPY websocket-proxy/requirements.txt requirements-websocket.txt
COPY websocket-proxy/websocket-proxy.py .

RUN pip install -r requirements.txt \
	&& pip install -r requirements-websocket.txt

EXPOSE 5006/udp

CMD [ "python" ]
