/*
 * Copyright 2011 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpleemail.AWSJavaMailTransport;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest;

/**
 * This sample demonstrates how to make basic requests to the Amazon Simple Email
 * Service using the the standard JavaMail API.
 * <p>
 * <b>Prerequisites:</b> You must have a valid Amazon Web Services developer
 * account, and be signed up to use Amazon Simple Email Service. For more information
 * on Amazon Simple Email Service, see http://aws.amazon.com/ses .
 * <p>
 * <b>Important:</b> Be sure to fill in your AWS access credentials in the
 * AwsCredentials.properties file before you try to run this sample.
 * http://aws.amazon.com/security-credentials
 */
public class AWSJavaMailSample {

	/*
	 * Important: Be sure to fill in an email address you have access to
	 *            so that you can receive the initial confirmation email
	 *            from Amazon Simple Email Service.
	 */
	private static final String TO = "nobody@nowhere.com";
	private static final String FROM = "nobody@nowhere.com";
	private static final String BODY = "Hello World!";
	private static final String SUBJECT = "Hello World!";

	public static void main(String[] args) throws IOException {

		/*
		 * Important: Be sure to fill in your AWS access credentials in the
		 * AwsCredentials.properties file before you try to run this sample.
		 * http://aws.amazon.com/security-credentials
		 */
		PropertiesCredentials credentials = new PropertiesCredentials(
				AWSJavaMailSample.class
						.getResourceAsStream("AwsCredentials.properties"));
		
        /*
         * SES requires that the sender and receiver of each message be
         * verified through the service. The verifyEmailAddress interface will
         * send the given address a verification message with a URL they can
         * click to verify that address.
         */
		AmazonSimpleEmailService email = new AmazonSimpleEmailServiceClient(credentials);		
        ListVerifiedEmailAddressesResult verifiedEmails = email.listVerifiedEmailAddresses();
		for (String address : new String[] { TO, FROM }) {		    
            if (!verifiedEmails.getVerifiedEmailAddresses().contains(address)) {
                email.verifyEmailAddress(new VerifyEmailAddressRequest().withEmailAddress(address));
                System.out.println("Please check the email address " + address + " to verify it");
                System.exit(0);
            }
		}
		
	   /*
		* Get JavaMail Properties and Setup Session
		*/
		Properties props = new Properties();

	   /*
	    * Setup JavaMail to use the Amazon Simple Email Service by
	    * specifying the "aws" protocol.
		*/
		props.setProperty("mail.transport.protocol", "aws");

		/*
		 * Setting mail.aws.user and mail.aws.password are optional. Setting
		 * these will allow you to send mail using the static transport send()
		 * convince method.  It will also allow you to call connect() with no
		 * parameters. Otherwise, a user name and password must be specified
		 * in connect.
		 */
		props.setProperty("mail.aws.user", credentials.getAWSAccessKeyId());
		props.setProperty("mail.aws.password", credentials.getAWSSecretKey());

		Session session = Session.getInstance(props);

		try {
			// Create a new Message
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(FROM));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
			msg.setSubject(SUBJECT);
			msg.setText(BODY);
			msg.saveChanges();

			// Reuse one Transport object for sending all your messages
			// for better performance
			Transport t = new AWSJavaMailTransport(session, null);
			t.connect();
			t.sendMessage(msg, null);

			// Close your transport when you're completely done sending
			// all your messages
			t.close();

		} catch (AddressException e) {
			e.printStackTrace();
			System.out.println("Caught an AddressException, which means one or more of your "
					+ "addresses are improperly formatted.");
		} catch (MessagingException e) {
			e.printStackTrace();
			System.out.println("Caught a MessagingException, which means that there was a "
					+ "problem sending your message to Amazon's E-mail Service check the "
					+ "stack trace for more information.");
		}
	}

}
