import os

for i in xrange(1, 137):
	command = ("curl http://freme.aksw.org/datasets/orcid/orcid-rdf%d.ttl.gz "
		"| zcat "
		"| curl -d @- -X PUT 'localhost:8080/api/datasets/orcid?format=TURTLE&properties=http%%3A%%2F2Forcid.org%%2Fns%%23otherName%%2Chttp%%3A%%2F%%2Fschema.org%%2Fname%%2Chttp%%3A%%2F%%2Fwww.w3.org%%2F2000%%2F01%%2Frdf-schema%%23label'" % i)
	print command
	os.system(command)