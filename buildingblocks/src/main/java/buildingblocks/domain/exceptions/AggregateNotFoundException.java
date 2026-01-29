package buildingblocks.domain.exceptions;

public class AggregateNotFoundException extends DomainException {
    public AggregateNotFoundException(String aggregateName, String id) {
        super(aggregateName + " with ID " + id + " was not found.");
    }}
