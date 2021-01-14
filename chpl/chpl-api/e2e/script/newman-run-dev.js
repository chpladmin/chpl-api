const async = require('async');
const newman = require('newman');
const path = require('path');

const collection_path = path.resolve('chpl/chpl-api/e2e/collections');
const reports_path = path.resolve('newman');
const data_path = path.resolve('chpl/chpl-api/e2e/data');

const commonOptions = {
    reporters: ['junit','cli'],
    insecure: true,
    environment: 'chpl/chpl-api/e2e/env/dev.postman_environment.json',
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

const jobs = [
    cb => newman.run(acbControllerTests,cb),
    cb => newman.run(developerControllerDataTests,cb),
    cb => newman.run(developerControllerNoDataTests,cb),
    cb => newman.run(productsControllerTests,cb),
    cb => newman.run(productVersionControllerTests,cb),
    cb => newman.run(statusControllerTests,cb),
    cb => newman.run(testinglabControllerTests,cb),
];

const responseCallback = (err) => {
    err && console.error(err);
};

async.series(jobs, responseCallback);
