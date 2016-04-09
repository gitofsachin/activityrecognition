/***************************************************************************
							ACTIVITY MONTIOR
					SACHIN S and SAJNA REMI CLERE
				Indian Institute of Science, Bangalore

			Source code to read sensor data from smartphone through TCP
***************************************************************************/

#include <stdio.h>      /* for printf() and fprintf() */
#include <sys/socket.h> /* for socket(), connect(), send(), and recv() */
#include <arpa/inet.h>  /* for sockaddr_in and inet_addr() */
#include <stdlib.h>     /* for atoi() and exit() */
#include <string.h>     /* for memset() */
#include <unistd.h>     /* for close() */

#define RCVBUFSIZE 320000   /* Size of receive buffer */

void ErrorExit(char *errorMessage);  /* Error handling function */

int main(int argc, char *argv[])
{
    int sock;
    struct sockaddr_in echoServerAddr; /* Echo server address */
    unsigned short echoServerPort;     /* Echo server port */
    char *servIP;                    /* Server IP address (dotted quad) */
    char *echoString;                /* String to send to echo server */
    char echoBuffer[RCVBUFSIZE];     /* Buffer for echo string */
    unsigned int echoStringLen;      /* Length of string to echo */
    int bytesRcvd, totalBytesRcvd;   /* Bytes read in single recv() 
                                        and total bytes read */

    if ((argc < 3) || (argc > 4))    /* Test for correct number of arguments */
    {
       fprintf(stderr, "Usage: %s <Smartphone IP> <Echo Word> [<Echo Port>]\n",
               argv[0]);
       exit(1);
    }

    servIP = argv[1];             /* First arg: server IP address (dotted quad) */
    echoString = argv[2];         /* Second arg: string to echo */

    if (argc == 4)
        echoServerPort = atoi(argv[3]); /* Use given port, if any */
    else
        echoServerPort = 8080;  /* 8080 is the default port in the app */

    /* Create a stream socket using TCP */
    if ((sock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0)
        ErrorExit("socket() failed");

    /* Server address structure */
    memset(&echoServerAddr, 0, sizeof(echoServerAddr));
    echoServerAddr.sin_family      = AF_INET;
    echoServerAddr.sin_addr.s_addr = inet_addr(servIP);
    echoServerAddr.sin_port        = htons(echoServerPort);

    /* Connection to echo server */
    if (connect(sock, (struct sockaddr *) &echoServerAddr, sizeof(echoServerAddr)) < 0)
        ErrorExit("connect() failed");

    echoStringLen = strlen(echoString);

    /* Send the string to the server */
    if (send(sock, echoString, echoStringLen, 0) != echoStringLen)
        ErrorExit("send() sent a different number of bytes than expected");

	/* Read application layer header from smartphone*/
    totalBytesRcvd = 0;
	int i,state=0,sizetoread=0;
	char ch;
	while(1)
    {
        if ((bytesRcvd = recv(sock, echoBuffer, 1, 0)) <= 0)
		{
            ErrorExit("recv() failed or connection closed prematurely");
		}
		else
		{
	        totalBytesRcvd += bytesRcvd;   /* Keep tally of total bytes */
	        echoBuffer[bytesRcvd] = '\0';  /* Terminate the string! */
			ch=echoBuffer[bytesRcvd-1];
			switch(state)
			{
				case 0: if(ch=='s')state=1;break;
				case 1: if(ch=='i')state=2;else state=0;break;
				case 2: if(ch=='z')state=3;else state=0;break;
				case 3: if(ch=='e')state=4;else state=0;break;
				case 4: if(ch=='s')state=5;
						else
						{
							sizetoread=sizetoread*10+ch-'0';
						}
						break;
				case 5: if(ch=='i')state=6;else state=10;break;
				case 6: if(ch=='z')state=7;else state=10;break;
				case 7: if(ch=='e')state=8;else state=10;break;
				case 8: if(ch=='n')state=9;else state=10;break;
				case 9: if(ch=='d')state=10;else state=10;break;
				default: printf("Unexpected case");break;
			}
			if(state==10)break;
		}
    }

	/* Read application data from smartphone */
	totalBytesRcvd=0;
	FILE *fptr;
	int nooflines=0;
	int readstate=0;
	while (totalBytesRcvd < sizetoread-1)
    {
        /* Receive up to the buffer size (minus 1 to leave space for
           a null terminator) bytes from the sender */
        if ((bytesRcvd = recv(sock, echoBuffer, 1, 0)) <= 0)
            ErrorExit("recv() failed or connection closed prematurely");
        totalBytesRcvd += bytesRcvd;   /* Keep tally of total bytes */
        echoBuffer[bytesRcvd] = '\0';  /* Terminate the string! */
		if(echoBuffer[0]=='\n')
		{
			nooflines++;
		}
		if(readstate==0)
		{
			fptr=fopen("gyro.txt","w");
		}
		else if(readstate==2)
		{
			fclose(fptr);
			fptr=fopen("linacc.txt","w");
		}
        switch(readstate)
		{
			case 0: readstate=1;break;
			case 1: if(nooflines==128)readstate=2;break;
			case 2: readstate=3;break;
			case 3: if(nooflines==256)readstate=4;break;
			default: break;
		}
		fprintf(fptr,"%s",echoBuffer);      /* Print the data to file*/
		if(readstate==4)
		{
			fclose(fptr);
		}
    }

    close(sock);
    exit(0);
}

void ErrorExit(char *errorMessage)
{
    perror(errorMessage);
    exit(1);
}
