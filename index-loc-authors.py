import requests, sys

firstDone = False
headers = {
     'X-Auth-Token': 'YOUR TOKEN HERE',
     'Content-Type': 'text/n3'
}


payload = ""

count = 1
for line in sys.stdin:
	if count % 10000 == 0:
		if firstDone:
			r = requests.put("http://api-dev.freme-project.eu/current/e-entity/freme-ner/datasets/loc-authors?informat=turtle&language=en&properties=http%3A%2F%2Fwww.w3.org%2F2004%2F02%2Fskos%2Fcore%23prefLabel%2Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2Ftitle", data=payload, headers=headers)
			print "Pushed %d" % count
			payload = ""
		else:
			r = requests.post("http://api-dev.freme-project.eu/current/e-entity/freme-ner/datasets?name=loc-authors&informat=turtle&description=The%20Library%20of%20Congress%20datasets%20provides%20author%20names.&language=en&visibility=public&properties=http%3A%2F%2Fwww.w3.org%2F2004%2F02%2Fskos%2Fcore%23prefLabel%2Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2Ftitle", data=payload, headers=headers)
			firstDone = True
			print "Pushed %d" % count
			payload = ""
		
	payload += line
	count += 1

if payload:
	print "there is something left"
	if firstDone:
			r = requests.put("http://api-dev.freme-project.eu/current/e-entity/freme-ner/datasets/loc-authors?informat=turtle&language=en&properties=http%3A%2F%2Fwww.w3.org%2F2004%2F02%2Fskos%2Fcore%23prefLabel%2Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2Ftitle", data=payload, headers=headers)
			print "Pushed %d" % count
			payload = ""
	else:
		r = requests.post("http://api-dev.freme-project.eu/current/e-entity/freme-ner/datasets?name=loc-authors&informat=turtle&description=The%20Library%20of%20Congress%20datasets%20provides%20author%20names.&language=en&visibility=public&properties=http%3A%2F%2Fwww.w3.org%2F2004%2F02%2Fskos%2Fcore%23prefLabel%2Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2Ftitle", data=payload, headers=headers)
		firstDone = True
		print "Pushed %d" % count
		payload = ""

print "finito"
