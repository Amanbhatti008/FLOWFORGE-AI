import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 200 }, // ramp up to 200 users
        { duration: '1m', target: 1000 }, // ramp up to 1000 users
        { duration: '30s', target: 0 },   // ramp down to 0 users
    ],
    thresholds: {
        http_req_duration: ['p(95)<200'], // 95% of requests should be below 200ms
        http_req_failed: ['rate<0.01'],   // Error rate should be less than 1%
    }
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
    // 1. Authenticate to get token (Optional if testing public endpoints)
    // For now, testing public or health endpoints, or assuming token is passed
    // Alternatively, just test workflow creation logic if auth is bypassed or hardcoded token
    
    // Simulate triggering a workflow
    let payload = JSON.stringify({
        inputData: {
            text: "Load testing " + Math.random()
        }
    });

    let params = {
        headers: {
            'Content-Type': 'application/json',
            // 'Authorization': 'Bearer YOUR_TOKEN'
        },
    };

    // Simulate hitting a workflow execution endpoint
    // In a real scenario, you'd fetch an actual workflow ID first
    let res = http.post(`${BASE_URL}/workflows/execute/load-test`, payload, params);

    check(res, {
        'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
        'transaction time < 200ms': (r) => r.timings.duration < 200,
    });

    sleep(1);
}
