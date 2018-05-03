import socket
import time

PORT = 4444
server = None
IP = None

while IP == None:
    #IP = socket.gethostbyname(socket.gethostname())
    IP = "192.168.50.56"
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.connect((IP, PORT))
    data = server.recv(1024).decode()
    if "<HAV_pi>" not in data:
        IP = None
    else:
        server.send("<HAV_ts>".encode())
    time.sleep(5)

while True:
    print(server.recv(2048).decode())