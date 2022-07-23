package gov.healthit.chpl.scheduler.job.changerequest.presenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ChangeRequestDetailsPresentationService {
    private Logger logger;
    private ExecutorService executorService;
    private ChangeRequestManager changeRequestManager;
    private Integer threadCount;

    public ChangeRequestDetailsPresentationService(ChangeRequestManager changeRequestManager,
            Integer threadCount,
            Logger logger) {
        this.changeRequestManager = changeRequestManager;
        this.threadCount = threadCount;
        this.logger = logger;
    }

    public void present(List<ChangeRequestSearchResult> searchResults, List<ChangeRequestCsvPresenter> presenters) {
        initializeExecutorService();

        try {
            logger.info("Getting all change request details...");
            List<CompletableFuture<Void>> crFutures = getAllChangeRequestFutures(searchResults, presenters);
            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(crFutures.toArray(new CompletableFuture[crFutures.size()]));

            // This is not blocking - presumably because it executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();
            logger.info("Completed getting all change request details.");
        } catch (Exception ex) {
            logger.error("Unexpected error getting all change request details.", ex);
        } finally {
            executorService.shutdown();
        }
    }

    private List<CompletableFuture<Void>> getAllChangeRequestFutures(List<ChangeRequestSearchResult> changeRequests,
            List<ChangeRequestCsvPresenter> crPresenters) {
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        for (ChangeRequestSearchResult changeRequest : changeRequests) {
            futures.add(CompletableFuture
                    .supplyAsync(() -> getChangeRequestDetails(changeRequest.getId()), executorService)
                    .thenAccept(crDetails -> crDetails.ifPresent(cr -> addToPresenters(crPresenters, cr))));
        }
        return futures;
    }

    private void addToPresenters(List<ChangeRequestCsvPresenter> crPresenters, ChangeRequest changeRequest) {
        crPresenters.stream()
            .forEach(crPresenter -> addToPresenter(crPresenter, changeRequest));
    }

    private void addToPresenter(ChangeRequestCsvPresenter crPresenter, ChangeRequest changeRequest) {
        try {
            crPresenter.add(changeRequest);
        } catch (IOException e) {
            logger.error(String.format("Could not write change request to CSV file: %s", changeRequest.getId()), e);
        }
    }

    private Optional<ChangeRequest> getChangeRequestDetails(Long changeRequestId) {
        try {
            return Optional.of(changeRequestManager.getChangeRequest(changeRequestId));
        } catch (EntityRetrievalException e) {
            logger.error(String.format("Could not retrieve changeRequest: %s", changeRequestId), e);
            return Optional.empty();
        }
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(threadCount);
    }
}
