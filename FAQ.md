#Frequently Asked Technical Questions.

# Technical Questions #

  * How important is the speed lost between a real socks connection and a socks via HTTP connection ?
  * What do I need to use socks via HTTP ?
  * Where can I get Java and a servlet capable Web Server ?
  * I am behind a Microsoft proxy that only allows NTLM authentication. What can I do ?
  * Can I use the source code ?



# How important is the speed lost between a real socks connection and a socks via HTTP connection ? #
> Socks via HTTP v0.16 reaches 60% of a real socks connection (ie 40% are lost). Many speed improvements will come with future versions.

## What do I need to use socks via HTTP ? ##

> SERVER : A permanent connection with full access to the Internet & Java installed.

> CLIENT : A computer located behind a proxy allowing http request. You need Java on the client.
## Where can I get Java and a servlet capable Web Server ? ##

> Java : Both JDK or JRE can be downloaded from http://www.java.sun.com.

> OPTIONAL : Servlet capable Web Server : Apache JServ, or Apache+Tomcat : http://www.apache.org.
## I am behind a Microsoft proxy that only allows NTLM authentication. What can I do ? ##

> Socks via HTTP does not support native NTLM authentication. But you can use NTLM Authorization Proxy Server. Install it then configure Socks via HTTP to use it as proxy. This just works fine.