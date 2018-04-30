import serial
import serial.tools.list_ports
from threading import Thread
import socket
import time

def listen(ser, server):
    while True:
        line = ser.readline()
        if line.decode() != "" and line.decode() != "\n":
            pline = line.decode().replace("\n", "")
            print(pline)

rbSer = None

PORT = 4444;
client = 0
address = 0

connectd = False

#COM PORT STUFF
ports = list(serial.tools.list_ports.comports())
for p in ports:
    rbSer = serial.Serial(p[0], 9600, timeout=10)
    line = rbSer.readline()
    print(line)
    rbSer.write("<HAV_pi>\n".encode())
    line = rbSer.readline()
    if "<HAV_rb>" in line.decode():
        print(line.decode())
        connectd = True

#SOCKET STUFF (SERVER)
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    IP = socket.gethostbyname(socket.gethostname())
    server.bind((IP, PORT))
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

thread = Thread(target=listen, args=(rbSer, server,))
thread.start()

while connectd == True:
    command = client.recv(1024).decode()
    rbSer.write(command.encode())
