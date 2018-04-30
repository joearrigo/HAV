import socket
import time

PORT = 4444
server = None
IP = None

while IP == None:
    #IP = socket.gethostbyname(socket.gethostname())
    IP = socket.gethostbyname("HAV_pi")
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.connect((IP, PORT))
    server.send("<HAV_ts>".encode())
    data = server.recv(1024).decode()
    if "<HAV_pi>" not in data:
        IP = None
    else:
        print("<SELF> Connected!")
    time.sleep(5)

while True:
    server.send(input().encode())