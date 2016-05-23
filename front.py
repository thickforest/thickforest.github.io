#!/usr/bin/python
#coding:utf-8
from Tkinter import *

def debug():
	import pdb
	pdb.set_trace()

def init():
	root = Tk()
	root.title('米充')
	frm_L = Frame(root)
	frm_L.pack(side=LEFT)
	mobile_nos = StringVar()
	lb_char = Listbox(frm_L, selectmode=EXTENDED, listvariable = mobile_nos, width=10, height=16)
	lb_char.pack(side = LEFT)
	for i in range(100):
		lb_char.insert(END, str(i*i))
	scrl_char = Scrollbar(frm_L)
        scrl_char.pack(side=RIGHT, fill=Y)
        lb_char.configure(yscrollcommand = scrl_char.set)
        scrl_char['command'] = lb_char.yview

	frm_R = Frame(root)
	frm_R.pack(side = RIGHT)
	list1 = StringVar()
	list1.set('222')
	menu1 = OptionMenu(frm_R, list1, '111', '222', '333')
	menu1.pack(side = LEFT)
	list2 = StringVar()
	list2.set('22222')
	menu2 = OptionMenu(frm_R, list2, '11111', '22222', '33333')
	menu2.pack(side = RIGHT)

	def printinfo(event):
		debug()
		print lb_char.curselection()
		print list1.get()
		print list2.get()

	btn = Button(root, text="提交")
	btn.pack(side=BOTTOM)
	btn.bind('<ButtonRelease-1>', printinfo)

	mainloop()

if __name__ == '__main__':
	init()
