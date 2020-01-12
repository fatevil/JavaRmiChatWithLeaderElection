

Console application for chatting. Java RMI used for implementation. Hirschberg-Sinclair algorithm for electing new leader.

Typical usage: 
- Initialize Master node that creates the networ
- Initialize Client node that join the network
- Start the discussion

Sending Chat messages:
- Write message to the console (ChatConsole object)
    - ChatConsole verifies leader availability (if unavailable, request fix)
    - ChatConsole requests copy of list of active nodes (leader is the owner of the EstablishedNetwork object)
    - ChatConsole sends the message with a prefix ("Name: ")
    
- Hearbeat
    - Object NodeImpl implements leader availability check (in a seperate thread)
    - if the network is unavailable, requests fix
    
- Network fix
    - the two nodes that lost their neighbour send neighbour inquiry message
    - non-broken nodes nodes pass the message
    - broken nodes connect to each other

- Leader Election
    - Hirschberg-Sinclair algorithm is implemented for electing new leader 
    - every node figuring out network unavailability (by heartbeat or ChatConsole) sends message about starting election and sends message with their own vote for themselves
    - message for leader election contains:
        - message direction (ring toplogy - right, left)
        - node identification (ip address, port, unique ID)
        - election phase
        - traveled distance
    - upon receiving message that the leader has been elected, all the nodes join the newly created network

============

Konzolová aplikace pro chatování. Implementace pomocí Java RMI. Oprava topologického modelu při ztrátě vůdce algoritmem Hirschberg-Sinclair. 

Typické použití:

- Inicializace prvního nodu, který vytvoří novou síť (EstablishedNetwork).
- Inicializace dalších nodů, které se do vytvořené sítě připojí.
- Začne diskuze

- Odeslání Chat zprávy
    - Uživatel napíše do konzole text (zpracování objektem ChatConsole)
    - ChatConsole ověří dostupnost leadera a pokud dostupný není, požádá o opravu
    - ChatConsole dále požádá o kopii listu aktivních účastníků sítě - objekt EstablishedNetwork vlastněný leadrem
    - ChatConsole každému účastníkovi pošle zprávu z konzole obohacenou o určený prefix se jménem
    
- Heartbeat
    - Objekt NodeImpl implementuje funkci pro zjišťování stavu sítě 
    - V odděleném vlákně kontroluje dostupnost objektu EstablishedNetwork aktuálního leadera
    - Pokud objekt není dostupný, požádá o opravu sítě     
    
- Oprava sítě
    - při ztrátě leadera každý node, který přišel o souseda odešle poptávku na nového souseda (ti jsou vždy dva)
    - při přijetí poptávky na souseda nody, které sousedy mají, přeposílají zprávu dál
        - ti rozbití nastaví jako souseda původce poptávky
    - následuje volba leadra
    
- Volba leadra
    - pro volbu leadera je implementován algoritmus Hirschberg-Sinclair
    - každý node při zjištění, že se síť rozpadla rozešle zprávu o začátku volby a dále pošle svůj vlastní hlas
    - zpráva s volbou vůdce obsahuje
        - směr zprávy
        - identifikátory původce zprávy (ip adresa, port, unikátní ID)
        - důvod zprávy: začátek voleb, volební prezentace vlastního nodu, oznámení o zvoleném nodu
        - fáze, ve které je hlasování
        - vzdálenost, kterou zpráva urazila
    - při přijetí zprávy o zvoleném vůdci všechny uzly vstoupí do nové sítě zprostředkované novým vůdcem