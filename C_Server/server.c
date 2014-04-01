/***********************************************************
Filename: server.c

Description: This is a simple implementation of an http
server process. It is intended to receive a message with
a filename as the content, search the working directory for 
the file, and then return the file as an http response. The
file requested is assumed to be a plain text file. Please note
that the filename must be preceded by a '/'. Please also note that
if this program is immediately restarted, the port may not be useable.
So if you intend to restart this program, please wait at least
one minute before doing so.

Author: I believe the original code was written by
Evan Baatarjav, but I am not entirely certain. It was
provided by him on the CSCE 3530 course website but had no author
specified.

Modified by: Manuel Gottardi

Last modified: April 1, 2014
************************************************************/

#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <stdio.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

#define SIZE 1000
#define BUFFERSIZE 1000
//#define SERVER_IP "192.168.1.5" // home
#define SERVER_IP "10.124.4.235" // UNT
#define SERVER_PORT 31588

int main ()
{
	int server_sockfd, client_sockfd;
	int server_len, client_len;
	int fdread, szread, szwrite, i, exit = 0;
	char buff[SIZE], file_requested[100];
	char *pstring;
	struct sockaddr_in server_address;
	struct sockaddr_in client_address;
	int bytesReceived = 0;
	char message[BUFFERSIZE];

	// Create an unnamed socket
	server_sockfd = socket (AF_INET, SOCK_STREAM, 0);
	if (server_sockfd==-1){
		perror("Create socket");
		return(0);
	}

	// Assign an address (name) to the socket
	server_address.sin_family = AF_INET;
	server_address.sin_addr.s_addr = inet_addr (SERVER_IP);
	server_address.sin_port = htons(SERVER_PORT);
	server_len = sizeof (server_address);
	bind (server_sockfd, (struct sockaddr *) &server_address, server_len);

	// Create a connection queue and wait for clients to arrive
	listen (server_sockfd, 10);

	while(1){
		// Make a connection when a request is received
		client_len = sizeof (client_address);
		client_sockfd = accept (server_sockfd, (struct sockaddr *) &client_address, &client_len);
		if (client_sockfd<0){
			perror("Accept connection");
			close(client_sockfd);
			continue;
		}
		printf ("server waiting\n");

		// Receive request from the client (only able to receive GET requests)
		bytesReceived = recv(client_sockfd, message, BUFFERSIZE, 0);
		message[bytesReceived] = '\0'; // properly terminate string
		printf ("received from %s\n%s", inet_ntoa (client_address.sin_addr), message);

		// Parse message for file requested
		pstring = strstr(message, "GET");
		if(pstring == NULL){
			printf("ERROR! User made request other than GET\n");
			write (client_sockfd, "HTTP/1.1 501 Not Implemented\n", 15);
			close (client_sockfd);
			continue;
		}
		pstring += 4;

		// Copy filename to file_requested	
		i = 0;
		file_requested[0] = '.';
		while(pstring[i] != ' ' && pstring[i] != '\n' && pstring[i] != EOF){
			file_requested[i+1] = pstring[i];
			i++;
		}
		file_requested[i+1] = '\0';
		//printf("File requested is: %s\n", file_requested);

		// Attempt to open file in READ ONLY mode
		fdread = open(file_requested, O_RDONLY); // Open the read file in read only mode
		if(fdread == -1) // If either file could not be opened/created, exit program with error message
		{
			printf("ERROR! Could not open the requested file.\n");
			write (client_sockfd, "HTTP/1.1 404 Not Found\n\n", 24);
			close (client_sockfd);
			continue;
		}

		// If file was successfully opened, stream it to the client after adding appropriate headers
		write(client_sockfd, "HTTP/1.1 200 OK\nContent-Type: text/plain; charset=us-ascii\nConnection: close\n\n", 78); 

		// Stream file
		while(exit != 1) // While exit flag has not been set
		{
			szread = read(fdread, buff, SIZE); // Read the first "chunk" of size SIZE from the file and store in buffer buff
			szwrite = write(client_sockfd, buff, szread); // Write to the client what was read
			if(szwrite < SIZE) // If the end of the file was reached, exit the loop
				exit = 1;
		}

		// Close all streams
		close(fdread);
		close(client_sockfd);
	}
	close (server_sockfd);
	return 0;
}
