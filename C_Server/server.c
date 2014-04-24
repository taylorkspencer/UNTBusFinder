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
	strcpy(location, "{ \"lat\": 33.168633, \"long\": -97.068325 }\0");
	char temp[BUFFERSIZE];
	char request[300], message_body[BUFFERSIZE];
	struct sockaddr_in server_address;
	struct sockaddr_in client_address;
	FILE *output;

	// Create an unnamed socket
	server_sockfd = socket (AF_INET, SOCK_STREAM, 0);
	if (server_sockfd==-1){
		//output = fopen("output.txt", "a");
		//fprintf(output, "\nCreate proxy socket\n") // File
		//fclose(output);
		perror("Create proxy socket"); // Terminal
		return(0);
	}

	// Assign an address (name) to the socket
	server_address.sin_family = AF_INET;
	server_address.sin_addr.s_addr = inet_addr (SERVER_IP);
	server_address.sin_port = htons(SERVER_PORT);
	server_len = sizeof (server_address);
	if(bind(server_sockfd, (struct sockaddr *) &server_address, server_len) == -1){
		//output = fopen("output.txt", "a");
		//fprintf(output, "\nSystem has not released port. Try again in a minute.\n\n"); // File
		//fclose(output);
		printf("\nSystem has not released port. Try again in a minute.\n\n"); // Terminal
		close(server_sockfd);
		return 0;
	}

	// Create a connection queue and wait for clients to arrive
	listen (server_sockfd, 4);

	// Main loop for the program
	while(1){
		// Connect to client
		// Make a connection when a request is received
		client_len = sizeof (client_address);
		client_sockfd = accept (server_sockfd, (struct sockaddr *) &client_address, &client_len);
		if (client_sockfd<0){
			//output = fopen("output.txt", "a");
			//fprintf(output, "\nAccept connection\n"); // File
			//fclose(output);
			perror("Accept connection"); // Terminal
			close (client_sockfd);
			continue;
		}
		//output = fopen("output.txt", "a");
		//fprintf(output, "Serverconnected to client.\n\n"); // File
		//fclose(output);
		printf ("Server connected to client.\n\n"); // Terminal

		// Receive request from the client
		memset(message, '\0', BUFFERSIZE);
		bytesReceived = recv(client_sockfd, message, BUFFERSIZE, 0);

		// If empty request, ignore
		if(bytesReceived < 1){
			//output = fopen("output.txt", "a");
			//fprintf(output, "Empty HTTP request. Ignoring.\n\n"); // File
			//fclose(output);
			printf("Empty HTTP request. Ignoring.\n\n"); // Terminal
			close(client_sockfd);
			continue;
		}
		//output = fopen("output.txt", "a");
		//fprintf(outlook, "Received message from %s\n\n%s\n\n", inet_ntoa(client_address.sin_addr), message); File
		//fclose(output);
		printf ("Received message from %s\n\n%s\n\n", inet_ntoa(client_address.sin_addr), message); // Terminal

		// Get request
		i = 0;
		if((pstring = strstr(message, "GET")) != NULL){
			//output = fopen("output.txt", "a");
			//fprintf(output, "Received GET request. Sending the following response:\n\n"); // File
			printf("Received GET request. Sending the following response:\n\n"); // Terminal
			write(client_sockfd, "HTTP/1.1 200 OK\r\n", 17);
			//fprintf(output, "HTTP/1.1 200 OK\r\n"); // File
			printf("HTTP/1.1 200 OK\r\n"); // Terminal
			write(client_sockfd, "Connection: close\r\n", 19);
			//fprintf(output, "Connection: close\r\n"); // File
			printf("Connection: close\r\n"); // Terminal
			write(client_sockfd, "Content-Type: application/json\r\n\r\n", 34); // Modify this to be accurate
			//fprintf(output, "Content-Type: application/json\r\n\r\n"); // File
			printf("Content-Type: application/json\r\n\r\n"); //Terminal
			write(client_sockfd, location, strlen(location));
			write(client_sockfd, "\r\n\r\n", 4);
			//fprintf(output, "%s\n\n", location); // File
			printf("%s\n\n", location); // Terminal
			//fclose(output);
			close(client_sockfd);
			continue;
		}

		else{
			if((pstring = strstr(message, "POST")) == NULL){
				//output = fopen("output.txt", "a");
				//fprintf(output, "Received unknown request type. Sending error response to client and discarding.\n\n"); // File
				printf("Received unknown request type. Sending error response to client and discarding.\n\n"); //Terminal
				write(client_sockfd, "HTTP/1.1 400 Bad Request\r\n", 26);
				write(client_sockfd, "Connection: close\r\n\r\n", 21);
				//fclose(output);
				close(client_sockfd);
				continue;
			}

			else{
				//output = fopen("output.txt", "a");
				//fprintf(output, "Received a POST request. Sending the following response:\n\n"); // File
				printf("Received a POST request. Sending the following response:\n\n"); // Terminal
				pstring = strstr(message, "\n\r\n");
				if(pstring == NULL){
					//fprintf(output, "No body. Discarding\n\n"); // String
					printf("No body. Discarding\n\n"); // Terminal
					write(client_sockfd, "HTTP/1.1 400 Bad Request\r\n", 26);
					write(client_sockfd, "Connection: close\r\n\r\n\r\n\r\n", 25);
					close(client_sockfd);
					//fclose(output);
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
				//fprintf(output, "HTTP/1.1 201 Created\r\n"); // File
				printf("HTTP/1.1 201 Created\r\n"); // Terminal
				write(client_sockfd, "Content-Type: text\r\n", 20);
				//fprintf(output, "Content-Type: text\r\n"); // File
				printf("Content-Type: text\r\n"); // Terminal
				write(client_sockfd, "Connection: close\r\n\r\n", 21);
				//fprintf(output, "Connection: close\r\n\r\n"); // File
				printf("Connection: close\r\n\r\n"); // Terminal
				write(client_sockfd, location, strlen(location));
				//fprintf(output, "%s\n\n", location); // File
				printf("%s\n\n"); // Terminal
				write(client_sockfd, "\r\n\r\n\0", 5);
				//fclose(output);
				close(client_sockfd);
				continue;
			}
		}
	}
	close (server_sockfd);
	return 0;
}
