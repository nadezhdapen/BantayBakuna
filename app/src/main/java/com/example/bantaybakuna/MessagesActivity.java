package com.example.bantaybakuna;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class MessagesActivity extends AppCompatActivity {

    private Toolbar toolbarMessages;
    private WebView webViewMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        toolbarMessages = findViewById(R.id.toolbarMessages);
        webViewMessages = findViewById(R.id.webViewMessages);

        // Setup Toolbar
        setSupportActionBar(toolbarMessages);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Vaccine Information");
        }

        // Configure WebView and load content
        configureWebView();
        loadCustomHtmlContent(); // Load our custom info page
    }

    // Configure basic WebView settings
    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        if (webViewMessages != null) {
            WebSettings webSettings = webViewMessages.getSettings();

            webViewMessages.setWebViewClient(new WebViewClient());
        }
    }

    private void loadCustomHtmlContent() {
        // Define the HTML content as a String
        String htmlData = "<html><head>" +
                "<style>" +
                "body { background-color:#FEF3E0; color:#3E2723; padding: 15px; font-family: sans-serif; line-height: 1.6; }" +
                "h2 { color: #8D6E63; border-bottom: 1px solid #D7CCC8; padding-bottom: 5px; }" +
                "h3 { color: #6D4C41; }" +
                "a { color: #EF6C00; text-decoration: none; font-weight: bold; }" +
                "ul { padding-left: 20px; }" +
                "li { margin-bottom: 10px; }" +
                "</style></head><body>" +

                "<h2>About the EPI Philippines</h2>" +
                "<p>The Expanded Program on Immunization (EPI) was established by the Department of Health (DOH) to ensure infants, children, and mothers have access to routinely recommended vaccines...</p>" + // Shortened for brevity

                "<h2>Why is Immunization Important?</h2>" +
                "<p>Vaccines work with your child's natural defenses to build protection against specific diseases...</p>" +
                "<ul>" +
                "<li>Protects your child from preventable diseases.</li>" +
                "<li>Prevents outbreaks in the community.</li>" +
                "<li>Saves lives and reduces healthcare costs.</li>" +
                "</ul>" +

                "<h2>Is it Mandatory in the Philippines?</h2>" +
                "<p>Yes, Republic Act No. 10152 mandates basic immunization services for infants and children up to five years old...</p>" + // Shortened

                "<h2>Learn More from Official Sources:</h2>" +
                "<ul>" +
                "<li><a href=\"https://www.pidsphil.org/home/wp-content/uploads/2024/11/2025-PIDSP-Immunization-Calendar.pdf\">PIDSP 2025 Immunization Calendar (PDF)</a></li>" +
                "<li><a href=\"https://doh.gov.ph/vaccination\">DOH Vaccination Program</a></li>" +
                "<li><a href=\"https://www.who.int/philippines/health-topics/immunization\">WHO Philippines - Immunization</a></li>" +
                "<li><a href=\"https://www.unicef.org/philippines/immunization\">UNICEF Philippines - Immunization</a></li>" +
                "<li><a href=\"https://www.officialgazette.gov.ph/2011/07/26/republic-act-no-10152/\">Republic Act No. 10152 (Official Gazette)</a></li>" +
                "<li><a href=\"https://situationofchildren.org/child-immunization\">Situation of Children: Child Immunization</a></li>" +
                "<li><a href=\"https://doh.gov.ph/press-release/vaccines-bring-us-closer-philippines-celebrates-world-immunization-week-to-honor-the-importance-of-life-saving-vaccinations/\">DOH: World Immunization Week Press Release</a></li>" +
                "<li><a href=\"https://www.unicef.org/philippines/stories/routine-immunization-children-philippines\">UNICEF Philippines: Routine Immunization Story</a></li>" +
                "<li><a href=\"https://www.econstor.eu/handle/10419/241052\">EconStor: Study on PH EPI Target Achievement</a></li>" +
                "</ul>" +
                "<p><i>Always consult your pediatrician or local health center for personalized advice regarding your child's vaccination schedule.</i></p>" +

                "</body></html>";

        if (webViewMessages != null) {
            webViewMessages.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (webViewMessages != null && webViewMessages.canGoBack()) {
                webViewMessages.goBack();
            } else {
                super.onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (webViewMessages != null && webViewMessages.canGoBack()) {
            webViewMessages.goBack();
        } else {
            super.onBackPressed();
        }
    }
}