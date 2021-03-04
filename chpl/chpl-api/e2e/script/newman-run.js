const async = require('async');
const newman = require('newman');
const path = require('path');

const collection_path = path.resolve('chpl/chpl-api/e2e/collections');
const reports_path = path.resolve('newman');
const data_path = path.resolve('chpl/chpl-api/e2e/data');

const commonOptions = {
  reporters: ['junit','cli'],
  insecure: true,
  environment: 'chpl/chpl-api/e2e/env/' + process.argv[2] + '.postman_environment.json',
};

const acbControllerTests = {
  ...commonOptions,
  collection: collection_path + '/acb-controller.postman_collection.json',
  reporter: {
    junit: {
      export: reports_path + '/acb-controller-tests.xml',
    },
  },
};

const apiKeyControllerTests = {
    ...commonOptions,
    collection: collection_path + '/api-key-controller.postman_collection.json',
    reporter: {
        junit: {
            export: reports_path + '/api-key-controller-tests.xml',
        },
    },
};

const certifiedProductControllerDataTests = {
  ...commonOptions,
  collection: collection_path + '/certified-product-controller.postman_collection.json',
  folder: 'GET : certified_product: data driven tests',
  iterationData: data_path + '/chplPrefixIdentifier-test-data.json',
  reporter: {
    junit: {
      export: reports_path + '/certified-product-controller-data-tests.xml',
    },
  },
};

const certifiedProductControllerNoDataTests = {
  ...commonOptions,
  collection: collection_path + '/certified-product-controller.postman_collection.json',
  folder: 'GET : Certified_products: non-data driven tests',
  reporter: {
    junit: {
      export: reports_path + '/certified-product-controller-nodata-tests.xml',
    },
  },
};

const developerControllerDataTests = {
  ...commonOptions,
  collection: collection_path + '/developer-controller.postman_collection.json',
  folder: 'GET : Developers: data driven tests',
  iterationData: data_path + '/developerIds-test-data.json',
  reporter: {
    junit: {
      export: reports_path + '/developer-controller-data-tests.xml',
    },
  },
};

const developerControllerNoDataTests = {
  ...commonOptions,
  collection: collection_path + '/developer-controller.postman_collection.json',
  folder: 'GET : Developers: non-data driven tests',
  reporter: {
    junit: {
      export: reports_path + '/developer-controller-nodata-tests.xml',
    },
  },
};

const jobControllerTests = {
  ...commonOptions,
  collection: collection_path + '/job-controller.postman_collection.json',
  reporter: {
    junit: {
      export: reports_path + '/job-controller-tests.xml',
    },
  },
};

const productsControllerTests = {
  ...commonOptions,
  collection: collection_path + '/products-controller.postman_collection.json',
  reporter: {
    junit: {
      export: reports_path + '/products-controller-tests.xml',
    },
  },
};

const productVersionControllerTests = {
  ...commonOptions,
  collection: collection_path + '/product-version-controller.postman_collection.json',
  reporter: {
    junit: {
      export: reports_path + '/product-version-controller-tests.xml',
    },
  },
};

const schedulerControllerTests = {
  ...commonOptions,
  collection: collection_path + '/scheduler-controller.postman_collection.json',
  reporter: {
    junit: {
      export: reports_path + '/scheduler-controller-tests.xml',
    },
  },
};

const statusControllerTests = {
  ...commonOptions,
  collection: collection_path + '/status-controller.postman_collection.json',
  reporter: {
    junit: {
      export: reports_path + '/status-controller-tests.xml',
    },
  },
};

const testinglabControllerTests = {
  ...commonOptions,
  collection: collection_path + '/testing-lab-controller.postman_collection.json',
  reporter: {
    junit: {
      export: reports_path + '/testing-lab-controller-tests.xml',
    },
  },
};

const searchviewControllerTests = {
  ...commonOptions,
  collection: collection_path + '/search-view-controller.postman_collection.json',
  reporter: {
    junit: {
      export: reports_path + '/search-view-controller-tests.xml',
    },
  },
};

const jobs = [
  cb => newman.run(acbControllerTests,cb),
  cb => newman.run(apiKeyControllerTests,cb),
  cb => newman.run(certifiedProductControllerDataTests,cb),
  cb => newman.run(certifiedProductControllerNoDataTests,cb),
  cb => newman.run(developerControllerDataTests,cb),
  cb => newman.run(developerControllerNoDataTests,cb),
  cb => newman.run(jobControllerTests,cb),
  cb => newman.run(productsControllerTests,cb),
  cb => newman.run(productVersionControllerTests,cb),
  cb => newman.run(statusControllerTests,cb),
  cb => newman.run(testinglabControllerTests,cb),
  cb => newman.run(schedulerControllerTests,cb),
  cb => newman.run(searchviewControllerTests,cb),
];

const responseCallback = (err) => {
  err && console.error(err);
};

async.series(jobs, responseCallback);
