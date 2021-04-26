# Read Me First

1. Compile application : mvn clean install
2. Run application : mvn spring-boot:run
3. Access swagger UI: http://localhost:8088/swagger-ui.html#/
4. Access product scrapper endpoint
   1. API get list of product: /handphone
      This will run web scrape process in the background with indicator status process in the response.
   2. API export to CSV: /handphone/exportCSV
