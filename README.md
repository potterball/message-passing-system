# Peer-to-Peer Messaging System

Distributed Systems course (CPSC 559) project. Refactored for CPSC 501.

`registry-1.0-SNAPSHOT` developed by Professor Nathaly Verwaal at the University of Calgary to test the functionality of the project.

Project `peer-to-peer-messaging-system` developed by Me(Ahmed Najjar).
<br><br>

# Table of contents

- [Introduction](#introduction)
- [Execution](#execution)
  - [On the same host machine](#host-machine)
- [Refactoring Report](#report)
  - [Refactoring 1](#1)
  - [Refactoring 2 (Major)](#2)
  - [Refactoring 3](#3)
  - [Refactoring 4](#4)
  - [Refactoring 5 (Major)](#5)
    <br><br>

# Introduction <a id="introduction"></a>

Each peer (running instance of my project) will participate in a Twitter-like system. The program allows a user to enter a snippet of text content through the command line, which will be shared will all known peers in the system(other running instances of my project or another students project) using UDP.

<strong>NOTE:</strong> The project currently can only communicate with other peers within the same network or running on the same host machine due to the limitations of the built-in InetAddress class and DatagramPacket class. Will update the codebase with improvements soon.
<br><br>

# Execution <a id="execution"></a>

Download the artifacts from my <a href="https://gitlab.cpsc.ucalgary.ca/ahmedalijamaal.najja/assignment1">gitlab</a>.
<br><br>

## On the same host machine <a id="host-machine"></a>

### <strong>Step 1:</strong> Run registry.

`java -cp registry-1.0-SNAPSHOT.jar com.example.Registry`

Will create an instance of the registry that oversees the communication between peers. On execution, will give you the [registry ip] and [registry port number].

### <strong>Step 2:</strong> Run initial peer.

`java -cp peer-to-peer-messaging-system-1.0-SNAPSHOT.jar ahmedalijamaal.najja.peertopeermessagingsystem.App -s [registry ip] -p [registry port number] -t [team name identifier]`

Every peer instance first communicates with the registry before it communicates with another peer.

### <strong>Step 3:</strong> Repeat Step 2 one or more times with different [team name identifier]s.

Recommended: 3-4 instances of the peer

### <strong>Step 4:</strong> After a peer has finished initial communication with the registry, you can start typing in messages on the command line and other peers will receive and display them.

### <strong>Step 5:</strong> To terminate peers, give the registry the input <em>done</em>.

The peer threads will begin to shutdown, an acknowledgement will be sent and then the peers will communicate with the registry one last time to obtain reports. Wait for this to complete and the registry and the peers will terminate on their own.
<br><br>

# Refactoring Report <a id="report"></a>

## Refactoring 1 <a id="1"></a>

### What code in which files was altered? What code in which files was the result of the refactoring?

App.java(lines 62; 293-300), MessageReceiver.java(lines 175 and 201), PeerSender.java(line 43) and SnippetManager.java(line 54).

Refer to commit: `edc4e049`

### What needed to be improved?

Code smell: Inappropriate Inticmacy

Other classes had direct access to App.java's shutdown AtomicBoolean to check if threads had to be closed while I just wanted them to have access to AtomicBoolean's get() and compareAndSet() methods.

### What refactoring was applied? What steps did I follow?

Refactoring applied: Encapsulate field

Steps:

- Create getters and setters for shutdown in App.java
- Use the created getters and setters in the other classes
- Change visibility of shutdown from protected to private

### How was the code tested?

shutdownTest() in MessageReceiverTest, PeerSenderTest and SnippetManagerTest test the appropriate value of shutdown from the getters successfully prevent the threads from reading or sending UDP packets.

### Why is the code better structured after the refactoring?

The data and the behaviour being in the same place in the code, makes it easier to maintain and develop if I wanted to check if for some reason shutdown isn't returning the value I am expecting.

### Does the result of the refactoring suggest or enable further refactorings?

Yes, the refactoring makes it easier to deal with any behaviours that must be added when shutdown is changed. All I would have to do this make the change in the compareAndSetShutdown() method in App.java and that would be propagated across the other classes.

---

## Refactoring 2 <a id="2"></a>

### What code in which files was altered? What code in which files was the result of the refactoring?

App.java(lines 26-35; 220-221; 231; and 240), SentPeer.java(lines 5-8; 10-13; 16-18; 21-22; 26; 28-30; 32-37; 41 and 46), PeerSender.java(lines 125-127), Snip.java(lines 21-23; 28-37; 46; 55; 59-62; 64; 73 and 85-90), Peer.java(lines 11-13; 16-18; 20-22; 46; 40; 64; 73; 82; 92-100 and 107), MessageReceiver(lines 47; 78; 190; 95; 107-108; 124; 131; 136; 195-200), RegistryCommunicator(lines 223-226) and ProcessAddress(new class) and Test Suite.

Refer to

- Branches: `main` and `refactor2`
- Commits: `077c41b0`, `1900eb5b`, `acb5a3c8`, `ac39b163`, `b1e97fe5`, `b7fd5cc9`, `fc4b6421`, `4688b699`, `be26f2e7` and `184447a5`

### What needed to be improved?

Code smell: Data clumps and duplicate code

The combination of an IP address and a port number represented a peer to communicate to or the registry but were treated as singular objects across Peer.java, SentPeer.java, Snip.java and App.java

### What refactoring was applied? What steps did I follow?

Refactoring applied: Extract Class

Steps:

- Create new class with fields IP address and port number as well as getters and setters
- Link older classes with the new class
- Get rid of data clumps that represent IP address and port number

### How was the code tested?

Tests in MessageReceiverTest, SnippetManagerTest and PeerSenderTest needed to pass as they were before the refactoring with the data in the packets not being changed.

### Why is the code better structured after the refactoring?

The data and the behaviour being in the same place in the code, makes it easier to maintain and develop like in the scenario where I needed to validate a certain peer (see <a id="4"> refactoring 4</a>).

### Does the result of the refactoring suggest or enable further refactorings?

Yes, see <a id="4"> refactoring 4</a>.

---

## Refactoring 3 <a id="3"></a>

### What code in which files was altered? What code in which files was the result of the refactoring?

App.java(lines 292-299), PeerSender.java(lines 74-76) and SnippetManager.java(lines 91-93).

Refer to commit: `bb9e8bb4`

### What needed to be improved?

Code Smell: Duplicate code

The result of the IP address and port number of a stored peer being the same as my private IP or Public IP was the similar and led to code duplication across methods.

### What refactoring was applied? What steps did I follow?

Refactoring applied: Consolidate Conditional Expression and Extract Method

Step:

- Combine conditionals using `and` and `or`. (Not visible in the commits for some reason)
- Extract conditional into a method
- Moved method to App.java which was a field in every other class

### How was the code tested?

checkPeerIsMeTest and moreThanOneActivePeerTest in SnippetManagerTest and PeerSenderTest make sure that when the process address (IP address and Peer) matches the peer itself then no message is sent out to that peer.

### Why is the code better structured after the refactoring?

By consolidating all operators and extracting the method, this complex expression is a new method with a name that explains the conditional’s purpose and is easily accessible since it's in a resource shared by all classes.

### Does the result of the refactoring suggest or enable further refactorings?

Yes, lots of similar conditionals in the codebase that could be consolidating and extracted away into method.

---

## Refactoring 4 <a id="4"></a>

### What code in which files was altered? What code in which files was the result of the refactoring?

App.java(lines 83-87; 202; 259-262), MessageReceiver.java(lines 61-63;68-76), RegistryCommunicator(lines 206-208; 213-221), ProcessAddress.java(new field and method) and Test Suite.

Refer to commits: `492e855a` and `77b6fafc`

### What needed to be improved?

Code Smell: Duplicate code

IP address and Port Number are validated a peer is stored and a command line argument are passed. The validation process is replicated in the thread dealing with received peers(before refactoring 5 in PeerHandlingThread) and the parseArguments() method of App.java.

### What refactoring was applied? What steps did I follow?

Refactoring applied: Extract method and move method

Steps:

- Extract the validation process into methods for IP validation and port validation with the same name in both classes.
- Then move the method as a static method into the ProcessAddress that stores IP adddress and port number since they are logically connected.

### How was the code tested?

### Why is the code better structured after the refactoring?

IP addresses and port numbers are not stored before they are validated now and their validation happens in the ProcessAddress class which stores the combination of IP address and port number so its easier develop to add more validation steps.

### Does the result of the refactoring suggest or enable further refactorings?

Not sure, but more stringent validation of IP address beyond just a string match would be a change I would like to bring.

---

## Refactoring 5 <a id="5"></a>

### What code in which files was altered? What code in which files was the result of the refactoring?

MessageReceiver.java(lines 29-158, 178-199, new method <em>delegateToThread()</em>), SnipHandler.java(new class), PeerHandler(new class) and StopHandler(new class)

Refer to

- Branches: `main` and `final_refactor`
- Commits: `794b3f92`, `90709e13`, `1f136c33`, `ebbb4997` and `229e4e5d`

### What needed to be improved?

Code smell: Switch statements and Inner Classes

### What refactoring was applied? What steps did I follow?

Refactoring applied: Concocted combination of Replace Type Code with Classes and Replace Conditional with Polymorphism but not really. (Had to do it because Replace Conditional with Polymorphism didn't completely apply to what I was trying to do)

Steps:

- Extracted Inner Classes and added an extra class implementing Runnable dealing with stop messages
- Moved the switch into a method
- Replaced the switch condition String into TYPE CODE that stored final static Strings
- Moved some of the code from the body of the switch into the extracted classes from the first step
- Have the switch only return objects.

### How was the code tested?

MessageReceiverTests pass only when the functionality of the switch is replicated perfectly else all of them fail.

### Why is the code better structured after the refactoring?

Moving the inner classes outside makes the structure of the class they belonged to earlier much simpler and now it is easier to deal with any new types of messages if we wanted to. All we would need to do is create a new class and create an instance of that class to the switch in delegateToThread() method.

### Does the result of the refactoring suggest or enable further refactorings?

Not sure, the Runnable interface makes it difficult to completely resort to a Replace Conditional with Polymorphism solution.
