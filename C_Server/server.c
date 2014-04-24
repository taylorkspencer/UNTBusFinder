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
	char *pstring, message[BUFFERSIZE], location[100];
	char temp[BUFFERSIZE];
	char request[300], message_body[BUFFERSIZE];
	struct sockaddr_in server_address;
	struct sockaddr_in client_address;
	FILE *output;

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

	memset(location, '\0', 100);

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
		//printf ("Server connected to client.\n\n"); // Debug

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
		//printf ("Received message from %s\n\n%s\n\n", inet_ntoa(client_address.sin_addr), message); // Long debug

		// Get request
		i = 0;
		if((pstring = strstr(message, "GET")) != NULL){
			output = fopen("output.txt", "a");
			fprintf(output, "Received get request. Sending the following response:\n\n"); // Debug
			write(client_sockfd, "HTTP/1.1 200 OK\r\n", 17);
			fprintf(output, "HTTP/1.1 200 OK\r\n");
			write(client_sockfd, "Connection: close\r\n", 19);
			fprintf(output, "Connection: close\r\n");
			write(client_sockfd, "Content-Type: application/x-www-form-urlencoded\r\n\r\n", 51); // Modify this to be accurate
			fprintf(output, "Content-Type: application/json\r\n\r\n");
			write(client_sockfd, location, strlen(location));
			write(client_sockfd, "\r\n\r\n", 4);
			fprintf(output, "%s\n\n", location);
			fclose(output);
			close(client_sockfd);
			continue;
		}

		else{
			if((pstring = strstr(message, "POST")) == NULL){
				output = fopen("output.txt", "a");
				fprintf(output, "Received unknown request type. Sending error response to client and discarding.\n\n"); // Debug
				write(client_sockfd, "HTTP/1.1 400 Bad Request\r\n", 26);
				write(client_sockfd, "Connection: close\r\n\r\n", 21);
				fclose(output);
				close(client_sockfd);
				continue;
			}

			else{
				output = fopen("output.txt", "a");
				fprintf(output, "Received a POST request. Sending the following response:\n\n"); // Debug
				pstring = strstr(message, "\n\r\n");
				if(pstring == NULL){
					fprintf(output, "No body. Discarding\n\n"); // Debug
					write(client_sockfd, "HTTP/1.1 400 Bad Request\r\n", 26);
					write(client_sockfd, "Connection: close\r\n\r\n\r\n\r\n", 25);
					close(client_sockfd);
					fclose(output);
					continue;
				}

				// Parse for and save the latitude
				pstring = strstr(message, "lat=");
				pstring += 4;
				memset(temp, '\0', BUFFERSIZE);
				i = 0;
				while(pstring[i] != '&'){
					temp[i] = pstring[i];
					i++;
				}
				memset(location, '\0', 100);
				strcpy(location, "{ \"lat\": \0");
				strcat(location, temp);
				strcat(location, ", \"long\": \0");

				// Parse for and save the longitude
				pstring = strstr(message, "long=");
				pstring += 5;
				strcat(location, pstring);
				strcat(location, " }\0");

				// Send response
				write(client_sockfd, "HTTP/1.1 201 Created\r\n", 22);
				fprintf(output, "HTTP/1.1 201 Created\r\n"); // Debug
				write(client_sockfd, "Content-Type: text\r\n", 20);
				fprintf(output, "Content-Type: text\r\n");
				write(client_sockfd, "Connection: close\r\n\r\n", 21);
				fprintf(output, "Connection: close\r\n\r\n"); // Debug
				write(client_sockfd, location, strlen(location));
				fprintf(output, "%s\n\n", location); // Debug
				write(client_sockfd, "\r\n\r\n\0", 5);
				fclose(output);
				close(client_sockfd);
				continue;
			}
		}
	}
	close (server_sockfd);
	return 0;
}
