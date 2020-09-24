const async = require('async');
const newman = require('newman');
const path = require('path');

const collection_path = path.resolve('chpl/chpl-api/e2e/collections');
const reports_path = path.resolve('newman');

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

const developerControllerTests = {
    ...commonOptions,
    collection: collection_path + '/developer-controller.postman_collection.json',
    reporter: {
        junit: {
            export: reports_path + '/developer-controller-tests.xml',
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
    cb => newman.run(developerControllerTests,cb),
    cb => newman.run(productsControllerTests,cb),
    cb => newman.run(statusControllerTests,cb),
    cb => newman.run(testinglabControllerTests,cb),
    cb => newman.run(searchviewControllerTests,cb),
];

const responseCallback = (err) => {
    err && console.error(err);
};

async.series(jobs, responseCallback);
