import serial
import serial.tools.list_ports
from threading import Thread
import socket
import time
import os

def listen(ser, client):
    while True:
        line = ser.readline()
        if line.decode() != "" and line.decode() != "\n":
            pline = line.decode().replace("\n", "")
            print(pline)
            client.send(("<HAV_pi>"+pline).encode())

def listen2(client, ser):
    while True:
        command = client.recv(2048).decode()
        if "<HAV_ts>" in command:
            command = command.replace("<HAV_ts>", "")
            print(command)
            if "reboot" in command:
                os.system('sudo shutdown -r now')
            else:
                print(command)
                ser.write(command.encode())

rbSer = None
pwSer = None

PORT = 4444
client = 0
address = 0

connectd = False
connectd2= False

#COM PORT STUFF
ports = list(serial.tools.list_ports.comports())
for p in ports:
    rbSer = serial.Serial(p[0], 9600, timeout=10)
    line = rbSer.readline()
    print("rb"+line.decode())
    rbSer.write("<HAV_pi>\n".encode())
    line = rbSer.readline()
    if "<HAV_rb>" in line.decode():
        print(line.decode())
        connectd = True
        break
    else:
        if "<HAV_pw>" in line.decode():
            print(line.decode())
            connectd2 = True
            break


#SOCKET STUFF (SERVER)
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
#print(socket.gethostname())
#IP = socket.gethostbyname("HAV_pi")
server.bind(("192.168.50.104", PORT))
server.listen(1)
while client == 0:
    while client == 0:
        client, address = server.accept()
    data = client.recv(1024).decode()
    if "<HAV_ts>" in data:
        client.send("<HAV_pi>".encode())
        print("<SELF> connected!")
    else:
        client = None

#MAIN BODY OF OPERATION

thread = Thread(target=listen, args=(rbSer, client,))
thread.start()

thread2 = Thread(target=listen2, args=(client, rbSer))
thread2.start()

thread3 = Thread(target=listen, args=(pwSer, client))
thread3.start()

while connectd == True or connectd2 == True:
    pass
