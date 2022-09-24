package fr.mistayan.java.trainings;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.util.Cookie;

import java.io.IOException;
import java.util.Set;

public class Main {

    private static void printHelp(){
        System.out.println("This tool will log you to stackoverflow.com and actualize your status");
        System.out.println("How to:");
        System.out.println("args: \"mail\" \"password\" \"username\"");
        System.out.println("mail : email to use on stackoverflow");
        System.out.println("password : password to use on stackoverflow");
        System.out.println("username : username associated to account, so we know we are logged in");
    }

    public static void main(final String [] args) {
        if (args.length < 3) {
            printHelp();
            System.exit(-1);
        }

        // for time saving purpose, I chose to use stackoverflow.com for the demonstration.
        final String site = "https://stackoverflow.com/";
        System.out.println("Generating Connector for current session");
        final Connector connector = new Connector();
        connector.setWebsite(site);
        connector.getPage(); // load website in browser
        System.out.printf("is %s on home page ? %s\n", args[2], connector.isloggedIn(args[2]));
        System.out.println("Looking for login Anchor in page");
        try {
            // forEach loop possible for search variations between sites
            final String search = "Log in";
            final HtmlAnchor anchor = connector.getPage().getAnchorByText(search);
            connector.setPage(anchor.click());  // will emulate user clicking, therefore, changing page's content
            System.out.printf("currently on \"%s\" page\n", connector.getPage().getTitleText());

            final Set<Cookie> before = connector.getCookies();
            if (connector.login(args[0], args[1])) {
                // check if operation were successful
                final Set<Cookie> after = connector.getCookies();
                try {
                    if (before != null && after != null && before.size() != after.size() && connector.isloggedIn(args[2])) {
                        System.out.printf("%s Successfully logged-in.\nSession Cookie: %s\n",
                                args[2], after.toArray()[1]);
                    } else {
                        System.err.println("Failed to log in. Cannot find username on page");
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.err.println("Failed to log in. No session cookie found");
                    return;
                }
            }

            if (connector.resolveCaptcha()){
                System.err.println("CAPTCHA !!");
            }

            // .... Do more stuff.

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
