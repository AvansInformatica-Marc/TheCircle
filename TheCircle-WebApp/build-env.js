const replace = require("replace-in-file")

async function addTimestamp() {
    const timestamp = new Date().toISOString()

    const options = {
        files: [
            'src/environments/environment.ts',
            'src/environments/environment.prod.ts',
        ],
        from: /timestamp:\s((\"(.*)\")|(\'(.*)\')|(null))/g,
        to: `timestamp: "${timestamp}"`,
        allowEmptyPaths: false,
    }

    await replace.replaceInFile(options)
}

async function addApiHost() {
    const host = process.env["THECIRCLE_HOST"] ?? "127.0.0.1"

    if (!host) return

    const options = {
        files: [
            'src/environments/environment.ts',
            'src/environments/environment.prod.ts',
        ],
        from: /apiHost:\s((\"(.*)\")|(\'(.*)\')|(null))/g,
        to: `apiHost: "${host}"`,
        allowEmptyPaths: false,
    }

    await replace.replaceInFile(options)
}

async function addApiPort() {
    const port = process.env["THECIRCLE_PORT"] ?? "3000"

    if (!port) return

    const options = {
        files: [
            'src/environments/environment.ts',
            'src/environments/environment.prod.ts',
        ],
        from: /apiPort:\s((\"(.*)\")|(\'(.*)\')|(null)|[0-9]*)/g,
        to: `apiPort: ${port}`,
        allowEmptyPaths: false,
    }

    await replace.replaceInFile(options)
}

async function main() {
    await addTimestamp()
    await addApiHost()
    await addApiPort()
}

main()
