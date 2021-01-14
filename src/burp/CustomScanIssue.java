package burp;

import java.net.URL;

class CustomScanIssue implements IScanIssue
{
 private IHttpService httpService;
 private URL url;
 private IHttpRequestResponse[] httpMessages;
 private String name;
 private String detail;

 public CustomScanIssue(
		 IHttpService httpService,
		 URL url,
         String name,
         String detail)
 {
	 this.url = url;
	 this.httpService = httpService;
     this.name = name;
     this.detail = detail;
 }
 
 @Override
 public URL getUrl()
 {
     return url;
 }

 @Override
 public String getIssueName()
 {
     return name;
 }

 @Override
 public int getIssueType()
 {
     return 0;
 }

 @Override
 public String getSeverity()
 {
     return "Information";
 }

 @Override
 public String getConfidence()
 {
     return "Firm";
 }

 @Override
 public String getIssueBackground()
 {
     return null;
 }

 @Override
 public String getRemediationBackground()
 {
     return null;
 }

 @Override
 public String getIssueDetail()
 {
     return detail;
 }

 @Override
 public String getRemediationDetail()
 {
     return null;
 }

 @Override
 public IHttpRequestResponse[] getHttpMessages()
 {
     return httpMessages;
 }

 @Override
 public IHttpService getHttpService()
 {
     return httpService;
 }
 
}