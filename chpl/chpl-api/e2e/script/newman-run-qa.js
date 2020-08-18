const path= require('path'),
async= require('async'),
newman= require('newman');

const collection_path= path.resolve('chpl/chpl-api/e2e/collections')
const reports_path= path.resolve('newman')
const data_path= path.resolve('chpl/chpl-api/e2e/data')
const environment_path= path.resolve('chpl/chpl-api/e2e/env')

const commonOptions = {
	reporters: ['junit','cli'],
	insecure: true,
	environment : environment_path + '/qa.postman_environment.json',
	reporter : {
		junit :{
			export: reports_path,
		}
	}
}

const acbControllerTests={
	...commonOptions,
	collection: collection_path + '/acb-controller.postman_collection.json',
	folder: 'GET: ACBs by id',
	iterationData: data_path + '/acb-id-test-data.json',
}

const acbNodataControllerTests={
	...commonOptions,
	collection: collection_path + '/acb-controller.postman_collection.json',
	folder: 'GET: All ACBs',
}

const apiControllerTests={
	...commonOptions,
	collection: collection_path + '/api-controller.postman_collection.json',
}

const developerControllerTests={
	...commonOptions,
	collection: collection_path + '/developer-controller.postman_collection.json',
}

const productsControllerTests={
	...commonOptions,
	collection: collection_path + '/products-controller.postman_collection.json',
}

const statusControllerTests={
	...commonOptions,
	collection: collection_path + '/status-controller.postman_collection.json',
}

const testinglabControllerTests={
	...commonOptions,
	collection: collection_path + '/testing-lab-controller.postman_collection.json',
}

const jobs = [
	cb => newman.run(acbControllerTests,cb),
	cb => newman.run(acbNodataControllerTests,cb),
	cb => newman.run(apiControllerTests,cb),
	cb => newman.run(developerControllerTests,cb),
	cb => newman.run(productsControllerTests,cb),
	cb => newman.run(statusControllerTests,cb),
	cb => newman.run(testinglabControllerTests,cb),
];

const responseCallback= (err, results) => {
	err && console.error(err); 
}

async.series(jobs, responseCallback);