/**
 * Triggered from a change to a Cloud Storage bucket.
 *
 * @param {!Object} event Event payload.
 * @param {!Object} context Metadata for the event.
 */
// Imports the Google Cloud client libraries
const vision = require('@google-cloud/vision');
const mysql = require('mysql');

const connectionName =
  process.env.INSTANCE_CONNECTION_NAME || 'meetup-aug-2019:asia-east1:meetup-aug-2019-db';
const dbUser = process.env.SQL_USER || 'meetup';
const dbPassword = process.env.SQL_PASSWORD || '';
const dbName = process.env.SQL_NAME || 'meetup2019';

const mysqlConfig = {
  connectionLimit: 1,
  user: dbUser,
  password: dbPassword,
  database: dbName,
};
if (process.env.NODE_ENV === 'production') {
  mysqlConfig.socketPath = `/cloudsql/${connectionName}`;
}

// Connection pools reuse connections between invocations,
// and handle dropped or expired connections automatically.
let mysqlPool;

function writeLablesToCloudSQL(imageUri, imageLabels) {
  // Initialize the pool lazily, in case SQL access isn't needed for this
  // GCF instance. Doing so minimizes the number of active SQL connections,
  // which helps keep your GCF instances under SQL connection limits.
  if (!mysqlPool) {
    mysqlPool = mysql.createPool(mysqlConfig);
  }

  const updateQuery = `Update message set image_labels='${imageLabels}' where image_uri='${imageUri}'`;
  console.log(updateQuery);

  mysqlPool.query(updateQuery, (err, results) => {
    if (err) {
      console.error(err);
    } else {
      console.log(results);
    }
  });

  // Close any SQL resources that were declared inside this function.
  // Keep any declared in global scope (e.g. mysqlPool) for later reuse.
};


// Creates a client
const client = new vision.ImageAnnotatorClient();

async function detectLabel(bucketName, fileName) {
  // Performs label detection on the gcs file
  console.log(`gs://${bucketName}/${fileName}`);
  const [result] = await client.labelDetection(
    `gs://${bucketName}/${fileName}`
  );

  const labels = result.labelAnnotations;
  let imageLabels;
  console.log('Labels:');
  imageLabels = labels.map(label => label.description).join(",");
  console.log(imageLabels);
  
  const imageUri = 'https://storage.cloud.google.com/' + `${bucketName}/${fileName}`;
  console.log('imageUri: ' + imageUri);
  writeLablesToCloudSQL(imageUri, imageLabels);
}

exports.processWithCloudVision = (event, context) => {
  const gcsEvent = event;
  const projectId = process.env.GCP_PROJECT;
  const bucketName = projectId;
  let fileName = gcsEvent.name;

  detectLabel(bucketName, fileName);

  console.log(`Project Id: ${projectId}`);
  console.log(`Processing file: ${gcsEvent.name}`);
};
