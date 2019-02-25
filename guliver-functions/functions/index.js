const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
const nodemailer = require('nodemailer')
const admin = require('firebase-admin');
admin.initializeApp( functions.config().firebase ); //initialize admin app instance from which Realtime Db changes can be made

//configure the email transport using default smtp transport and gmail account
const gmailEmail = functions.config().gmail.email;
const gmailPassword = functions.config().gmail.password;
const mailTranspot = nodemailer.createTransport({

    service: 'gmail',
    auth: {
        user: gmailEmail,
        pass: gmailPassword,
    },
});

//send email when user requests a ride
exports.sendEmailToGuliver = functions.database.ref('/customerRequest').onWrite(async (change) =>{
    const snapshot = change.after();
    const dataVal = snapshot.val();

    console.log("Data is ", dataVal);

    /*
    get the request details to send together with the email
    Users/Customers/uid/
                        -phone
                        - name
    */



    const mailOptions = {
        from: '"Guliver" <guliverdev@gmail.com>',
        to: 'kungusamuel90@gmail.com'
    };
});
