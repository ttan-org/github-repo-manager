FROM maven:3.8.6-eclipse-temurin-17

RUN addgroup --gid 1000 build && adduser --uid 1000 --gid 1000 --disabled-password --gecos "" build && \

# BROKEN KEYS, we install the binary directly now
#    curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg && \
#    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | tee /etc/apt/sources.list.d/github-cli.list > /dev/null && \
    wget https://github.com/cli/cli/releases/download/v2.14.7/gh_2.14.7_linux_amd64.deb && \
    apt-get install ./gh_2.14.7_linux_amd64.deb && \

    apt-get update && \
    apt-get install git gettext-base unzip -y
