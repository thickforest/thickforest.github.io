#!/usr/bin/python

import socket
import time
host = 'localhost'
port = 90
client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.setsockopt(socket.SOL_SOCKET, socket.SO_KEEPALIVE, 1)
client.setsockopt(socket.SOL_TCP, socket.TCP_KEEPCNT, 2)
client.setsockopt(socket.SOL_TCP, socket.TCP_KEEPIDLE, 30)
client.setsockopt(socket.SOL_TCP, socket.TCP_KEEPINTVL, 10)
client.connect((host, port))
client.send('hello world\r\n'.encode())
time.sleep(100000000)
