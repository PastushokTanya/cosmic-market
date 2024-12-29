FROM wiremock/wiremock:latest

# Copy the mapping mock files
COPY mappings /home/wiremock/mappings

# Expose the WireMock on external 8081 port (port 8080 is already used by the Spring application)
EXPOSE 8080 8081

# No need to override CMD; the default works fine