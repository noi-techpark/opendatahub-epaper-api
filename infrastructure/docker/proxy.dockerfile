FROM python:3.8-alpine

RUN mkdir -p /code
WORKDIR /code

COPY proxy/requirements.txt .
COPY proxy/proxy.py .

RUN pip install -r requirements.txt

EXPOSE 5006/udp

CMD [ "python" ]
