const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
const nodemailer = require('nodemailer')
const admin = require('firebase-admin');
admin.initializeApp( functions.config().firebase ); //initialize admin app instance from which Realtime Db changes can be made

//configure the email transport using default smtp transport and gmail account
const gmailEmail = functions.config().gmail.email;
const gmailPassword = functions.config().gmail.password;

const mailTransport = nodemailer.createTransport({

    service: 'Gmail',
    auth: {
        user: gmailEmail,
        pass: gmailPassword,
    },
});
console.log("created mailTransport");

//send email when user requests a ride
exports.sendEmailToGuliver = functions.database.ref('/customerRequest')
    .onCreate( async (change, context) => {

    //get the userID
    const userId = context.auth.uid;
    console.log("UserId is ", userId);


    return admin.database().ref('/Users/Customers/' + userId ).once("value")
      .then( snapshot => {
          var username;
          const phoneNumber = snapshot.val().phone;

          username ? snapshot.val().name !== null : "no name yet";

          //send email here
               /*
            get the request details to send together with the email
            Users/Customers/uid/
                                -phone
                                - name
            */
            const mailOptions = {
                from: '"Gulivery" <guliverdev@gmail.com>',
                to: 'kungusamuel90@gmail.com'
                };

                mailOptions.subject = "Ride request";
                mailOptions.text = "A ride request has been made by " +username+ " with phone number "+phoneNumber+ " ;"

            try{
                mailTransport.sendMail(mailOptions);
                console.log("Email sent")
            } catch(error){
                console.error("There was an error sending the message", error);
            }

          return console.log("PhoneNumber: ", phoneNumber);
      }).catch( err => {
          return console.log("Error", err);
      });

   });
