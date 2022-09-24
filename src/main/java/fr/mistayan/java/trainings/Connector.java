package fr.mistayan.java.trainings;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connector {
    private final WebClient webClient;
    private String website;
    private HtmlPage currentPage;

    public Connector() {
        this.webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        setBrowser();
    }

    public void setWebsite(String target){
        this.website = target.charAt(target.length() - 1) == '/' ? target : target + '/';
    }

    final protected void setBrowser(){
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.waitForBackgroundJavaScript(10000);
    }

    final public Set<Cookie> getCookies(){
        try {
            return webClient.getCookies(new URL(website));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    public void setPage(HtmlPage page){
        currentPage = page;
    }


    /*
    * automatically loads website on first call. return current page otherwise
    */
    public final HtmlPage getPage() {
        try {
            return currentPage == null ? (HtmlPage) webClient.getPage(this.website) : currentPage;
        } catch (IOException e) {
            System.err.printf("OOPS ! could not load %s page.", website);
            return currentPage;
        }
    }

    public Boolean login(final String login, final String pwd) throws IOException {
        if (getPage() == null){
            throw new RuntimeException("Not on any page");
        }

        final HtmlForm loginForm = getLoginForm();
        if (loginForm == null){
            System.err.println("form not found");
            return false;
        }

        // search and fill Form's Fields, simulating user clicking fields
        final HtmlEmailInput loginField  = loginForm.getInputByName("email");
        final HtmlPasswordInput pwdField  = loginForm.getInputByName("password");
        loginField.click();
        loginField.type(login);
        pwdField.click();
        pwdField.type(pwd);
        try {
            // once again, we could iter through a list of "usual button names"
            final String buttonName = "submit-button";
            final HtmlButton button = loginForm.getButtonByName(buttonName);
            if (button == null) {
                return false;
            }
            setPage(button.click());
            return true;
        } catch (IOException e) {
            System.out.println("Button not found.");
            throw new RuntimeException(e);
        }
    }

    private HtmlForm getLoginForm(){
        HtmlForm loginForm = null;
        final List<HtmlForm> pageForms = currentPage.getForms();
        final String formName = "login";
        for (HtmlForm field : pageForms){
            if (field.getMethodAttribute().equals("POST") && field.getId().contains(formName)){
                loginForm = field;
            }
        }
        return loginForm;
    }

    public Boolean isloggedIn(final String username){
        Pattern pattern = Pattern.compile(username);
        Matcher matcher = pattern.matcher(getPage().asXml());
        return matcher.find();
    }

    public Boolean resolveCaptcha(){
        Pattern pattern = Pattern.compile("captcha");
        Matcher matcher = pattern.matcher(getPage().asXml());
        return matcher.find();
    }
}
