const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
 exports.locations = functions.https.onRequest((request, response) => {
  var lat = request.query.lat;
  var lng = request.query.lng;
  var zoom = request.query.zoom;

  var result = [];
  result.push({lat: lat, lng:lng});

  response.send({locations: result});
 });

 exports.publish = functions.https.onRequest((request, response) => {
  response.sendStatus(200);
 });
