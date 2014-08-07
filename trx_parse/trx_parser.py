 #!/usr/bin/env python

import shlex, subprocess, re, glob, os, time
import httplib, socket

from os import listdir
from os.path import getmtime
from collections import deque
from optparse import OptionParser

# sudo easy_install pip
# [sudo] pip install python-dateutil
from dateutil import parser 

# [sudo] pip install watchdog
from watchdog.observers import Observer  
from watchdog.events import PatternMatchingEventHandler  

global virt_path, virt_log_path, virt_log_offset
virt_path = "./"
virt_log_path = virt_path + "var/lib/virtuoso/db/"
# virt_log_name = "virtuoso20140728082809.trx"
virt_log_offset = 0

global rsine_host, rsine_port
rsine_host = "127.0.0.1"
rsine_port = "2221"

global debug, total_nr_triples, trx_parsed_files, from_beginning, sleep, trx_files_to_parse, last_ex
debug = False
sleep = 1
total_nr_triples = 0
trx_files_to_parse = deque()
trx_parsed_files = [] # [{String path, int offset, float cmtime}, float mtime ...]
last_ex = False


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
	global last_ex
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
		last_ex = False
	except socket.error as ex:
		if not last_ex or last_ex.errno != ex.errno:
			print ex
			print "Error connecting to rsine service."
		last_ex = ex
	except httplib.BadStatusLine as ex:
		if not last_ex or last_ex.errno != ex.errno:
			print ex
			print "Lost connection to rsine service."
		last_ex=ex
	return False
# end placeRestCall


# parses <file> beginning at position <offset>, return last <offset> 
def parse(file, offset):
	global total_nr_triples
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
			total_nr_triples += triple_count
	if ok:
		return new_offset
	else: 
		return 0
# end parse


def handleFile(path):
	trx_parsed_file = search("path", path, trx_parsed_files)

	mtime = os.path.getmtime(path)
	cmtime = time.ctime(mtime)
	
	if len(trx_parsed_file) > 0: 
		print "parsing, starting at " + `trx_parsed_file[0]['offset']` + ", " + path
		new_offset = parse(path, trx_parsed_file[0]['offset'])
		if new_offset != trx_parsed_file[0]['offset']:
			trx_parsed_file[0]['offset'] = new_offset
			print "new_offset: " + `new_offset`
	else:
		print "parsing (new file), starting at 0, " + path 
		offset = 0
		new_offset = parse(path, offset)
		trx_parsed_files.append({'path': path, 'offset': new_offset, 'cmtime': cmtime})
		if new_offset != 0:
			print "new_offset: " + `new_offset`
#end handleFile


def parse_cmd_line():
	global virt_path, virt_log_path, rsine_host, rsine_port
	usage = "usage: python %prog -v <virtuoso_path> -l <virtuoso_trx_log_path> -r <rsine_host> -p <rsine_port>"
	parser = OptionParser(usage=usage)
	parser.add_option("-v", "--virtuoso_path", dest="virt_path",
	                  help="The path to the virtuoso install directory.")
	parser.add_option("-l", "--virt_log_path", dest="virt_log_path", 
	                  help="The path where the virtuoso transactions logs are written.")
	parser.add_option("-r", "--rsine_host", dest="rsine_host", 
	                  help="The hostname of the server wehre the rsine service is running.")
	parser.add_option("-p", "--rsine_port", dest="rsine_port", 
	                  help="The purt the rsine services listens on.")
	(options, args) = parser.parse_args()
	
	if options.rsine_host:
		rsine_host = options.rsine_host
	print "rsine_host: " + rsine_host

	if options.rsine_port:
		rsine_port = options.rsine_port
	print "rsine_port: " + `rsine_port`

	if options.virt_path:
		virt_path = options.virt_path
	print "virt_path: " + virt_path

	if options.virt_log_path:
		virt_log_path = options.virt_log_path
	print "virt_log_path: " + virt_log_path
#end parse_cmd_line


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
		# print event.src_path, event.event_type  # print now only for degug
		if not event.src_path in trx_files_to_parse:
			trx_files_to_parse.append(event.src_path)
		# print trx_files_to_parse

	def on_modified(self, event):
		self.process(event)

	def on_created(self, event):
		self.process(event)

parse_cmd_line()
observer = Observer()
observer.schedule(MyHandler(), virt_log_path)
observer.start()
try:
	while True:
		if trx_files_to_parse:
			last_ex = False
			handleFile(trx_files_to_parse.popleft())
		time.sleep(sleep)
except KeyboardInterrupt:
	observer.stop()
	print "total # of triples: " + `total_nr_triples`
observer.join()