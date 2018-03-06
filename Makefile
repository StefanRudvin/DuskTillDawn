HOME="$(shell pwd)"

RM_FLAGS="-f"

mud:
	javac mud/Edge.java; \
	javac mud/MUD.java; \
	javac mud/Vertex.java; \
    javac mud/MUDServerMainline.java; \
    javac mud/MUDServiceInterface.java; \
    javac mud/MUDServiceImpl.java; \
    javac mud/MUDClient.java

mudclean:
	cd mud; \
	rm $(RM_FLAGS) *.class *~; \
	cd $(HOME)