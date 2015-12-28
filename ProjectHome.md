# Socks via HTTP is a program converting SOCKS requests into HTTP requests and tunnelling them through HTTP proxies if needed. #

**The** SOCKS protocol allows programs to traverse firewalls on any port number and is used by many popular programs, like Napster, MSN Messenger, CRT(telnet client) and many others.

**Many** companies restrict firewall traversals only to HTTP requests, disabling SOCKS proxy.

**Socks via HTTP** provides a miniature SOCKS server for the SOCKS client, performing its connection through an HTTP proxy to a remote server, which establishes the real connection.

_Socks via HTTP is 100% Java, and can run on any OS._

## I - How it works ##
![http://socksviahttp.googlepages.com/socksviahttp_howItWorks.png](http://socksviahttp.googlepages.com/socksviahttp_howItWorks.png)

###  ###
## II - As the program is 100% Java, you can use any OS combinaison you want: ##
  * Server part 2b on Linux, Client part 2a on Windows.
  * Both Server part and client part on Linux.
  * Both Server part and client part on Windows.
  * Server part 2b on Windows, Client part 2a on Linux.
## III -  IV - Advanced description: ##
**1.The client part**

> The client part of Socks via HTTP acts as a socks server. Your program (IRC, Telnet or whatever) connects to this socks server, thinking it is speaking with a real socks server.
The socks via HTTP client communicates the socks via HTTP server using HTTP protocol.
# The HTTP packets are zipped on the fly to speed up network transfer.

**2 - Server part**

> The server parts manages the real connections. As you know, HTTP is a disconnected protocol, ie you create a request, send it, and you got a response.

> There is no connection context (I suppose here that the proxy you have to bypass does not support keep alive). As a consequence, the context handling is the job of the Socks via HTTP server part.

> The server part manages a HashTable containing all the opened connections. Each connection has an unique id. This id is sended by the Socks via HTTP client part for each request.





