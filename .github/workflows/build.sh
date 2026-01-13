cp -rf ./tests/checkstyle.xml ./checkstyle.xml
cp -rf ./tests/suppressions.xml ./suppressions.xml
mvn enforcer:enforce -Denforcer.rules=requireProfileIdsExist -P check --no-transfer-progress &&
mvn verify -P check,coverage --no-transfer-progress
# Docker build временно отключен
# && docker compose -f docker-compose.yml build
