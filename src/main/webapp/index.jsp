<html>
<head>
<title>SubNot Testing</title>
</head>
<body>

<h1>Subscription and Notification Testing System</h1>

<form action="testing" method="POST">
<table style="width:80%" >
<!-- <tr><td>Sparql Proxy</td>
<td>Endpoint:<input type="text" name="endpoint" value="http://10.0.0.85:8890/sparql" size="100"><br>
    ProxyUrl:<input type="text" name="proxyUrl" value="http://localhost:8080/subnot-testing/sparql" size="100"></td>
</tr> -->
<tr><td>Notification Service</td>
<td>
 <select>
  <option value="RsineService">RsineService</option>
 </select>
<table>
<tr><td>Service URL</td>
<td><input type="text" name="rsineUrl" value="http://localhost:2221" size="100"></td>
</tr>
</table> 
</td>
</tr>

<tr><td>Sparql Simulator</td>
<td>
 <select name="simulator">
  <option value="SimpleSparqlSimulator">SimpleSparqlSimulator</option>
  <option value="SupplyChainSimulator" selected="selected">SupplyChainSimulator</option>
 </select>
 <div class="visible" id="SupplyChainSimulatorParams">
    url:      <input type="text" name="SupplyChainSimulatorUrl" value="http://localhost:9000" size="100"><br/>
    frequency:<input type="text" name="SupplyChainSimulatorFrequency" value="1.0" size="4">
 </div>
</td>
</tr>
<tr><td></td>
<td>
<input type="checkbox" name="autoStop" value="true" checked="checked"> Stop the process in <input type="text" name="duration" value="60" size="4"> minutes
<input type="hidden" name="action" value="run">
</td>
</tr>
<tr><td></td>
<td>
<input type="submit" value="Run">
</td></tr>
</table>
</form>

</body>
</html>