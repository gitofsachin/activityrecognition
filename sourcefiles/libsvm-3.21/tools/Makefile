all: mydata mytraining output

mydata: testfilecreate.o
	octave --silent testfilecreate.o mydata 1000

mytraining: testfilecreate.o
	octave --silent testfilecreate.o mytraining 200

output: mydata mytraining commands
	./commands

clean:
	rm -f mydata_* mytraining_* gridoutput scalepars

aclean:
	rm -f mydata* mytraining* gridoutput scalepars output
