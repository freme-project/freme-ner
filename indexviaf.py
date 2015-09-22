import requests, sys

firstDone = False

payload = ""
count = 1
for line in sys.stdin:
	if count % 100000 == 0:
		if firstDone:
			r = requests.put("http://localhost:8080/api/datasets/viaf?format=N-TRIPLES&properties=http%3A%2F%2Fschema.org%2Fname%2Chttp%3A%2F%2Fschema.org%2FfamilyName%2Chttp%3A%2F%2Fschema.org%2FgivenName%2Chttp%3A%2F%2Fschema.org%2FfamilyName", data=payload)
			payload = ""
		else:
			r = requests.post("http://localhost:8080/api/datasets?name=viaf&format=N-TRIPLES&properties=http%3A%2F%2Fschema.org%2Fname%2Chttp%3A%2F%2Fschema.org%2FfamilyName%2Chttp%3A%2F%2Fschema.org%2FgivenName%2Chttp%3A%2F%2Fschema.org%2FfamilyName", data=payload)
			firstDone = True
			payload = ""

		print "Pushed %d" % count
		
	payload += line
	count += 1