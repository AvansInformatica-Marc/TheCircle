# Build
FROM gradle:7.4-jdk11-jammy as build

ARG THECIRCLE_HOST
ENV THECIRCLE_HOST=${THECIRCLE_HOST}

ARG THECIRCLE_PORT
ENV THECIRCLE_PORT=${THECIRCLE_PORT}

ENV GRADLE_USER_HOME=/usr/local/.gradle

RUN wget --output-document=android-cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip
ENV ANDROID_HOME=/usr/local/.android/sdk
RUN mkdir -p ${ANDROID_HOME}
RUN unzip -q android-cmdline-tools.zip -d ${ANDROID_HOME}
ENV PATH="${ANDROID_HOME}/cmdline-tools/bin/:${PATH}"
RUN echo y | sdkmanager --install "cmdline-tools;latest" --sdk_root=${ANDROID_HOME}
RUN echo y | sdkmanager --install "tools" "build-tools;33.0.0" "platform-tools" "platforms;android-32" "extras;android;m2repository" --sdk_root=${ANDROID_HOME}

WORKDIR /usr/local/app

COPY . /usr/local/app/
RUN ./gradlew assembleDebug

# Run
FROM nginx:latest

COPY --from=build /usr/local/app/app/build/outputs/apk/debug/ /usr/share/nginx/html

EXPOSE 80
