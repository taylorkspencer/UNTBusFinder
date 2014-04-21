#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <stdio.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

#define BUFFERSIZE 500
#define SERVER_IP "192.168.1.19" // Raspberry Pi
#define SERVER_PORT 31588

int main (){
	int server_sockfd, client_sockfd, bytesReceived, server_len;
	int client_len, i, j;
	int word_size;
	char *pstring, message[BUFFERSIZE], location[BUFFERSIZE];
	char temp[BUFFERSIZE];
	char request[300], message_body[BUFFERSIZE];
	struct sockaddr_in server_address;
	struct sockaddr_in client_address;	

	// Create an unnamed socket
	server_sockfd = socket (AF_INET, SOCK_STREAM, 0);
	if (server_sockfd==-1){
		perror("Create proxy socket"); // Debug
		return(0);
	}

	// Assign an address (name) to the socket
	server_address.sin_family = AF_INET;
	server_address.sin_addr.s_addr = inet_addr (SERVER_IP);
	server_address.sin_port = htons(SERVER_PORT);
	server_len = sizeof (server_address);
	if(bind(server_sockfd, (struct sockaddr *) &server_address, server_len) == -1){
		printf("\nSystem has not released port. Try again in a minute.\n\n"); // Debug
		close(server_sockfd);
		return 0;
	}

	// Create a connection queue and wait for clients to arrive
	listen (server_sockfd, 1000);

	memset(location, '\0', BUFFERSIZE);

	// Main loop for the program
	while(1){
		// Connect to client
		// Make a connection when a request is received
		client_len = sizeof (client_address);
		client_sockfd = accept (server_sockfd, (struct sockaddr *) &client_address, &client_len);
		if (client_sockfd<0){
			perror("Accept connection"); // Debug
			close (client_sockfd);
			continue;
		}
		printf ("Server connected to client.\n\n"); // Debug

		// Receive request from the client
		memset(message, '\0', BUFFERSIZE);
		bytesReceived = recv(client_sockfd, message, BUFFERSIZE, 0);

		// If empty request, ignore
		if(bytesReceived < 1){
			printf("Empty HTTP request. Ignoring.\n\n"); // Debug
			close(client_sockfd);
			continue;
		}
		//printf("Received message from client.\n\n"); // Short debug
		printf ("Received message from %s\n\n%s\n\n", inet_ntoa(client_address.sin_addr), message); // Long debug

		// Get request
		i = 0;
		if((pstring = strstr(message, "GET")) != NULL){
			printf("Received get request. Sending the following response:\n\n"); // Debug
			write(client_sockfd, "HTTP/1.1 200 OK\r\n", 17);
			printf("HTTP/1.1 200 OK\r\n");
			write(client_sockfd, "Connection: close\r\n", 19);
			printf("Connection: close\r\n");
			write(client_sockfd, "Content-Type: application/x-www-form-urlencoded\r\n\r\n", 51); // Modify this to be accurate
			printf("Content-Type: application/x-www-form-urlencoded\r\n\r\n");
			write(client_sockfd, location, strlen(location));
			printf("%s\n\n", location);
			close(client_sockfd);
			continue;
		}

		else{
			if((pstring = strstr(message, "POST")) == NULL){
				printf("Received unknown request type. Sending error response to client and discarding.\n\n"); // Debug
				write(client_sockfd, "HTTP/1.1 400 Bad Request\r\n", 26);
				write(client_sockfd, "Connection: close\r\n\r\n", 21);
				close(client_sockfd);
				continue;
			}

			else{
				printf("Received a POST request. Sending the following response:\n\n"); // Debug
				pstring = strstr(message, "\n\r\n");
				if(pstring == NULL){
					printf("No body. Discarding\n\n"); // Debug
					write(client_sockfd, "HTTP/1.1 400 Bad Request\r\n", 26);
					write(client_sockfd, "Connection: close\r\n\r\n\r\n\r\n", 25);
					close(client_sockfd);
					continue;
				}

				pstring += 3;
				memset(location, '\0', BUFFERSIZE);
				strcpy(location, pstring);
				write(client_sockfd, "HTTP/1.1 201 Created\r\n", 22);
				printf("HTTP/1.1 201 Created\r\n");
				write(client_sockfd, "Connection: close\r\n\r\n", 21);
				printf("Connection: close\r\n\r\n");
				close(client_sockfd);
				printf("Saved the following location data: %s\n\n", location); //Debug
				continue;
			}
		}
	}
	close (server_sockfd);
	return 0;
}