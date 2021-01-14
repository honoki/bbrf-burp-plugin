package burp;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class BurpExtender implements IExtensionStateListener, IScannerCheck, ITab, IContextMenuFactory {
	
	/*
	 * What this extension should do:
	 * 
	 *  - UI
	 *  	- show a tab to configure the bbrf AWS lamba portal location
	 *  	- or, alternatively, point to a local copy of the bbrf-client?
	 *  	- specify the program name to be used with bbrf (dropdown?)
	 *  - passive:
	 *  	- parse hostnames in all responses
	 *  	- match against in scope client-side (to reduce unnecessary requests to the api gateway)
	 *   	- match against known domains client-side? this requires keeping a copy in memory or otherwise 1 extra request every time, not ideal;
	 *   	- push all matches to bbrf to handle
	 */
	
	
	private Pattern DOMAIN_PATTERN = Pattern.compile("((?:[\\w-]+\\.)*(?:[\\w-]{1,63})(?:\\.(?:\\w{2,4})))(?::\\d{2,5})?(:?$|[/\\s\">&])");
	protected IBurpExtenderCallbacks callbacks;
	private IExtensionHelpers helpers;
	private ArrayList<String> all_domains = new ArrayList<>();
	private PrintWriter stdout;
	
	protected String program_name;
	protected String bbrf_py;
	
	protected BBRFProjectSettings settings;
	
	// gui elements
	public BBRFTab myPanel;
	
	public void registerExtenderCallbacks (IBurpExtenderCallbacks callbacks){
		
		callbacks.setExtensionName ("BBRF for Burp");
		this.helpers = callbacks.getHelpers();
		this.stdout = new PrintWriter(callbacks.getStdout(), true);
		this.callbacks = callbacks;
		
		// store project settings in site map
		settings = new BBRFProjectSettings(this, "bbrf");
		
		// load existing settings
		this.loadSettings();
		
		// unload resources when this extension is removed;
		callbacks.registerExtensionStateListener(this);
		
		// and register the passive scanner to start parsing domains from responses;
		callbacks.registerScannerCheck(this);
		
		// register the right-click menu:
		callbacks.registerContextMenuFactory(this);
		
		// ui stuff
		myPanel = new BBRFTab(this);
		
		// add a new tab where configuration can be set
		callbacks.addSuiteTab(this);
	}
	
	private class ThreadedRequest implements Callable<byte[]>  {

		private IHttpService service;
		private byte[] request;
		
		public ThreadedRequest(IHttpService service, byte[] request) {
			this.service = service;
			this.request = request;
		}
		
		@Override
		public byte[] call() throws Exception {
			IHttpRequestResponse response = callbacks.makeHttpRequest(this.service, this.request);
	    	return response.getResponse();
		}
		
	}
	
	
//	private String bbrf_gateway(String cmd) {
//		
//		if(bbrf_service == null) {
//			return null;
//		}
//		
//		ArrayList<String> headers = new ArrayList<String>();
//		
//		headers.add("POST "+bbrf_gateway_url+" HTTP/1.1");
//		headers.add("Host: "+bbrf_service.getHost());
//		headers.add("Content-Type: application/x-www-form-urlencoded");
//		
//		byte[] request  = helpers.buildHttpMessage(headers, helpers.stringToBytes("task="+cmd));
//		
//		logger(helpers.bytesToString(request));
//		
//		ThreadedRequest background = new ThreadedRequest(bbrf_service, request);
//		ExecutorService executor = Executors.newSingleThreadExecutor();
//		Future<byte[]> response = executor.submit(background);
//		
//	
//			byte[] response_text;
//			try {
//				response_text = response.get(1000, TimeUnit.MILLISECONDS);
//				return helpers.bytesToString(response_text);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ExecutionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (TimeoutException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//			
//			return null;
//		
//	}
	
	protected String bbrf(String cmd) {
		
		ArrayList<String> command = new ArrayList<String>();
		command.add("python");
		command.add(bbrf_py);
		for(String c : cmd.split(" ")) {
			command.add(c);
		}
		
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);
		
		Process process;
		String output = "";
		
		try {
			process = processBuilder.start();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String s;
			while ((s = stdInput.readLine()) != null) {
				output += s+"\n";
			}

			while ((s = stdError.readLine()) != null) {
                logger(s);
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	private void loadSettings() {
		this.bbrf_py = callbacks.loadExtensionSetting("client-location");
//		this.bbrf_gateway_url = callbacks.loadExtensionSetting("api-gateway-url");
//		this.program_name = callbacks.loadExtensionSetting("program-name");
		
		this.program_name = settings.loadProjectSetting("program-name");
	}
	
	protected void saveSettings() {
		// callbacks.saveExtensionSetting("api-gateway-url", bbrf_gateway_url);
		// callbacks.saveExtensionSetting("program-name", program_name);
		callbacks.saveExtensionSetting("client-location", bbrf_py);
		
		settings.saveProjectSetting("program-name", program_name);
	}
	
	protected void testBBRFClient() {
		this.bbrf_py = myPanel.textField_2.getText();
		logger("Testing bbrf client:");
		logger(bbrf("programs"));
	}

	@Override
	public void extensionUnloaded() {
		// TODO Any extensions that start background threads or open system resources (such as files or database
		// connections) should register a listener and terminate threads / close resources when the extension is unloaded.
	}

	@Override
	public List<IScanIssue> doPassiveScan(IHttpRequestResponse baseRequestResponse) {
		
		// only do this if a program has been selected
		if(program_name == null) return null;
		
		String response = helpers.bytesToString(baseRequestResponse.getResponse());
		
		Matcher m = DOMAIN_PATTERN.matcher(response);
		ArrayList<String> new_domains = new ArrayList<>();
		
		while (m.find()) {
			String domain = m.group(1);
			
			if(!all_domains.contains(domain)) {
				
				logger("Found new domain "+domain);
				
				all_domains.add(domain);
				new_domains.add(domain);
			}
		}
		
		// feed all new domains to bbrf
		if(new_domains.size() > 0) {
			String result = bbrf("domain add "+String.join(" ", new_domains)+" -s burp --show-new -p "+program_name);
			logger(result);
			
			if(result.contains("[NEW]")) {
				List<IScanIssue> issues = new ArrayList<>(1);
		        issues.add(new CustomScanIssue(
		        		baseRequestResponse.getHttpService(),
		                helpers.analyzeRequest(baseRequestResponse).getUrl(),
		                "BBRF - new domain(s) added",
		                "Identified and added new domains: \n"+result
		        ));
		        return issues;
		    }
		}
		
		// never generate an issue - except maybe an informative if new domains were found?
		return null;
	}

	@Override
	public List<IScanIssue> doActiveScan(IHttpRequestResponse baseRequestResponse,
			IScannerInsertionPoint insertionPoint) {
		// no active scans performed by bbrf
		return null;
	}

	@Override
	public int consolidateDuplicateIssues(IScanIssue existingIssue, IScanIssue newIssue) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	// Logging helper functions
	private void logger(String out) {
		stdout.println(out);
	}

	@Override
	public String getTabCaption() {
		return "BBRF";
	}

	@Override
	public Component getUiComponent() {
		return myPanel;
	}

	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		ArrayList<JMenuItem> menu = new ArrayList<JMenuItem>();
		
		JMenuItem sendToBBRF = new JMenu("Send to BBRF");
		JMenuItem sendDomains = new JMenuItem("Domains");
		JMenuItem sendUrls = new JMenuItem("URLs");
		
		IHttpRequestResponse[] selected = invocation.getSelectedMessages();
		
		sendDomains.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				ArrayList<String> domains = new ArrayList<String>();
				
				for(IHttpRequestResponse req : selected) {
					String domain = helpers.analyzeRequest(req).getUrl().getHost();
					if(!domains.contains(domain)) domains.add(domain);
				}
				
				if(domains.size() > 0) logger(bbrf("domain add "+String.join(" ", domains)+ " -s burp"));
				
			}
		});
		
		sendUrls.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				ArrayList<String> urls = new ArrayList<String>();
				
				for(IHttpRequestResponse req : selected) {
					String url = helpers.analyzeRequest(req).getUrl().toString();
					if(!urls.contains(url)) urls.add(helpers.analyzeRequest(req).getUrl().toString());
				}
				
				if(urls.size() > 0) logger(bbrf("url add "+String.join(" ", urls)+ " -s burp"));
				
			}
		});
		
		sendToBBRF.add(sendDomains);
		sendToBBRF.add(sendUrls);
		menu.add(sendToBBRF);
		
		return menu;
	}
		
}
