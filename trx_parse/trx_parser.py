 #!/usr/bin/env python

import shlex, subprocess, re, glob, os, time
import httplib, socket

from os import listdir
from os.path import getmtime

# [sudo] pip install python-dateutil
from dateutil import parser 

# [sudo] pip install watchdog
from watchdog.observers import Observer  
from watchdog.events import PatternMatchingEventHandler  

global virt_path, virt_log_path, virt_log_name, virt_log_offset
virt_path = "/opt/virtuoso-opensource-version-develop7/"
virt_log_path = virt_path + "var/lib/virtuoso/db/"
virt_log_name = "virtuoso20140728082809.trx"
virt_log_offset = 0

global rsine_host, rsine_port
rsine_host = "192.168.33.1"
rsine_port = "2221"

global debug, total_nr_triples, trx_parsed_files, from_beginning, now, sleep
debug = False
from_beginning = False
sleep = 1
now = time.time()
total_nr_triples = 0
trx_parsed_files = [] # [{String path, int offset, float cmtime}, float mtime ...]


def getFilesForPattern(pattern = virt_log_path + "*.trx"):
	trx_files_list = []
	trx_file_names = glob.glob(pattern)
	for trx_file_name in trx_file_names:
		tmp_dict = {}
		tmp_dict['path'] = trx_file_name
		mtime = os.path.getmtime(trx_file_name)
		tmp_dict['mtime'] = mtime
		tmp_dict['cmtime'] = time.ctime(mtime)
		trx_files_list.append(tmp_dict)
	if debug:
		print trx_files_list
	# sort ascending
	trx_files_list = sorted(trx_files_list, key=lambda k: parser.parse(k['cmtime']))
	return trx_files_list 
# end getFilesForPattern


# search for <key> with <term> in <list> and returns a list with [<element>]
def search(search_key, search_term, search_list):
	return [element for element in search_list if element[search_key] == search_term]
# end search



# wrap in <...> if string starts with "http"
def gtlt(string):
	if string.startswith("http"): 
		return "<" + string + ">"
	else:
		return "\"" + string + "\""
# end gtlt


# do a rest call to rsine 
def placeRestCall(op, s, p, o):
	if op == "I":
		operation = "add"
	elif op == "D":
		operation = "remove"
	body    = "changeType=" + operation + "\n"
	body   += "affectedTriple=" + gtlt(s) + " " + gtlt(p) + " " + gtlt(o) + " ."
	headers = {"Content-Type": "text/plain", 
			   "Accept": "text/plain"}

	conn = httplib.HTTPConnection(rsine_host, rsine_port)

	try:
		conn.request("POST", "", body, headers)
		res = conn.getresponse()
		if res.status == 200:
			return True
		else:
			print body
			print res.status, res.reason	
			return False
	except socket.error as ex:
		print ex
		print "Error connecting to rsine service."
	except httplib.BadStatusLine as ex:
		print ex
		print "Lost connection to rsine service."
	return False
# end placeRestCall


# parses <file> beginning at position <offset>, return last <offset> 
def parse(file, offset):
	new_offset = 0
	offset = int(float(offset))
	cmdline = virt_path + "/bin/isql 1111 dba dba \"EXEC=elds_read_trx('" + file + "', " + `offset` + "); exit;\""
	args = shlex.split(cmdline)

	output = subprocess.check_output(args)
	triple_count = 0
	ok = True
	for line in output.splitlines():
		# find "operation g s p o"
		match = re.search('([DI])\s+(\S+)\s+(\S+)\s+(\S+)\s+(.*)', line) 
		if match:
			triple_count += 1
			operation = match.group(1)
			g = match.group(2)
			s = match.group(3)
			p = match.group(4)
			o = match.group(5)
			if not placeRestCall(operation, s, p, o):
				ok = False

		# find "offset"
		match = re.search('(\d+)\s+BLOB.*', line) 
		if match:
			new_offset = match.group(1)
	
	if triple_count > 0:
		print "# of send triples: " + `triple_count` + "; all ok: " + `ok`
	if ok:
		return new_offset
	else: 
		return 0
# end parse

class MyHandler(PatternMatchingEventHandler):
    patterns = ["*.trx"]

    def process(self, event):
        """
        event.event_type 
            'modified' | 'created' | 'moved' | 'deleted'
        event.is_directory
            True | False
        event.src_path
            virt_log_path
        """
        # the file will be processed there
        print event.src_path, event.event_type  # print now only for degug

        

    def on_modified(self, event):
        self.process(event)

    def on_created(self, event):
        self.process(event)

observer = Observer()
observer.schedule(MyHandler(), virt_log_path)
observer.start()
try:
	while True:
		time.sleep(sleep)
except KeyboardInterrupt:
	observer.stop()
observer.join()

# Main loop
loop = 0
while True:
	loop += 1
	
	if debug:
		print "Loop: " + `loop`
	
	trx_files_list = getFilesForPattern()

	trx_file_index = 0

	for trx_file in trx_files_list:
		trx_file_index += 1
		trx_parsed_file = search("path", trx_file['path'], trx_parsed_files)

		path = trx_file['path']
		cmtime = trx_file['cmtime']
		mtime = trx_file['mtime']
		
		if len(trx_parsed_file) > 0: 
			if trx_parsed_file[0]['cmtime'] != cmtime:
				print "parsing (cmtime differs), starting at " + `trx_parsed_file[0]['offset']` + " - " + path
				new_offset = parse(path, trx_parsed_file[0]['offset'])
				if new_offset != trx_parsed_file[0]['offset']:
					trx_parsed_file[0]['offset'] = new_offset
					print "new_offset: " + `new_offset`
			else:
				if debug:
					print "skipping file (no mtime change) - " + path
		else:
			if not from_beginning and mtime < now:
				print "skipping file as modified prior to start - " + path
				trx_parsed_files.append({'path': path, 'offset': 0, 'cmtime': cmtime})
				continue
			
			print "parsing (new file), starting at 0 - " + path 
			offset = 0
			new_offset = parse(path, offset)
			trx_parsed_files.append({'path': path, 'offset': new_offset, 'cmtime': cmtime})
			if new_offset != trx_parsed_file[0]['offset']:
				print "new_offset: " + `new_offset`

	print "sleeping " + `sleep` + "s"
	time.sleep(sleep)
