#!/usr/bin/python
#coding:utf-8
from Tkinter import *
import tkMessageBox
import requests
import re
import os, sys
import threading
import time
PHONE_LIST_FILE = '当前端口本机号文件.txt'
ALL_RCV_MSG_FILE = '/root/接收记录.txt'

def debug():
	import pdb
	pdb.set_trace()

DB_lock = threading.RLock()
DB = {}

def update_mobile_list():
	global phones_pos
	while True:
		if not os.path.exists(PHONE_LIST_FILE):
			continue
		phones_pos = dict([line.split() for line in open(PHONE_LIST_FILE, 'rb').readlines()])
		phones = [line.split()[1] for line in open(PHONE_LIST_FILE, 'rb').readlines()]
		DB_lock.acquire()
		mobiles = dict(filter(lambda item: item[1]['online'] == True, DB.items())).keys()
		phones.sort()
		mobiles.sort()
		if tuple(phones) != phone_list_widget.get(0, END):
			for phone in phone_list_widget.get(0, END):
				DB[phone]['online'] = False
			for phone in phones:
				DB[phone]['online'] = True
			DB_lock.release()
			phone_list_widget.delete(0, END)
			for phone in phones:
				phone_list_widget.insert(END, phone)

def execute_command(line):
	global alert_msg
	fields = line.split()
	if len(fields) == 0:
		return (100, "", "nothing?")
	cmd = fields[0]
	args = fields[1:]
	if cmd not in ['clear_msg', 'read_msg', 'is_online', 'online_mobiles', 'push_status', 'alert']:
		return (101, "", "command not in the list!")
	if cmd == 'clear_msg':
		if len(fields) != 2:
			return (102, "", "clear_msg must has one argment!")
		mobile = args[0]
		if mobile not in DB:
			return (103, "", "mobile %s no in DB!" % mobile)
		else:
			DB_lock.acquire()
			n = len(DB[mobile]['msg'])
			DB[mobile]['msg'] = []
			DB_lock.release()
			return (0, n, "clear_msg done!"}
	elif cmd == 'read_msg':
		if len(fields) != 2:
			return (102, "", "read_msg must has one argment!")
		mobile = args[0]
		if mobile not in DB:
			return (103, "", "mobile %s no in DB!" % mobile)
		else:
			DB_lock.acquire()
			msgs = DB[mobile]['msg']
			DB_lock.release()
			return (0, msgs, "get msg success!"}
	elif cmd == 'is_online':
		if len(fields) != 2:
			return (102, "", "is_online must has one argment!")
		mobile = args[0]
		if mobile not in DB:
			return (103, "", "mobile %s no in DB!" % mobile)
		else:
			DB_lock.acquire()
			b = DB[mobile]['online']
			DB_lock.release()
			return (0, b, "check online success!"}
	elif cmd == 'online_mobiles':
		if len(fields) != 2:
			return (102, "", "online_mobiles must has no argment!")
		DB_lock.acquire()
		mobiles = dict(filter(lambda item: item[1]['online'] == True, DB.items())).keys()
		DB_lock.release()
		return (0, mobiles, "check online success!"}
	elif cmd == 'push_status':
		if len(fields) != 3:
			return (102, "", "push_status must has two argment!")
		mobile = args[0]
		if mobile not in DB:
			return (103, "", "mobile %s no in DB!" % mobile)
		else:
			DB_lock.acquire()
			DB[mobile]['status'] = args[1]
			DB_lock.release()
			return (0, "", "push_status done!")
	elif cmd == 'alert':
		if len(fields) != 2:
			return (102, "", "alert has at least one argment!")
		alert_msg = args[0]
		print alert_msg
		return (0, "", "set alert_msg done!"}
	

sock = None
def get_command_from_peer():
	global sock, command_queue
	import socket
	while True:
		sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
		try:
			sock.connect(('localhost', 8000))
		except socket.error, e:
			print e
			time.sleep(1)
			continue
		data = ''
		while True:
			new_data = sock.recv(1024)
			if new_data == '':
				print 'read end'
				sock.close()
				break
			data += new_data
			while True:
				line_end_pos = data.find('\n')
				if line_end_pos == -1:
					break
				line, data = data.split('\n', 1)
				# process line
				code, result, msg = execute_command(line)
				ret_data = {"code":code, "result": result, "msg":msg}
				sock.send(json.dumps(ret_data) + '\n')
			# end while True
		# end while True
	# end while True

if __name__ == '__main__':

	# 定时读入猫池新号码
	update_mobile_widget()
	
	# 定时检查是否需要弹出提示框
	check_alert_msg()

	# 读取所有MSG，响应等待短信验证码的线程
	execute_command_thread = threading.Thread(target = get_command_from_peer, args = (), name='EXECUTE_COMMAND')
	execute_command_thread.setDaemon(True)
	execute_command_thread.start()
