gsap.registerPlugin(ScrollTrigger);

document.addEventListener('DOMContentLoaded', () => {
    initHeroAnimations();
    initScrollAnimations();
    initProductCardAnimations();
});

function initHeroAnimations() {
    const heroTitle = document.getElementById('heroTitle');
    const heroSubtitle = document.getElementById('heroSubtitle');
    const heroButtons = document.querySelector('.hero-buttons');
    
    if (heroTitle) {
        gsap.from(heroTitle, {
            duration: 1,
            y: 50,
            opacity: 0,
            ease: 'power3.out'
        });
    }
    
    if (heroSubtitle) {
        gsap.from(heroSubtitle, {
            duration: 1,
            y: 30,
            opacity: 0,
            delay: 0.3,
            ease: 'power3.out'
        });
    }
    
    if (heroButtons) {
        // Set initial state
        gsap.set(heroButtons.children, { opacity: 0, y: 20 });
        
        // Animate in
        gsap.to(heroButtons.children, {
            duration: 0.8,
            y: 0,
            opacity: 1,
            delay: 0.6,
            stagger: 0.2,
            ease: 'power3.out',
            clearProps: 'all' // Clear inline styles after animation
        });
    }
}

function initScrollAnimations() {
    gsap.utils.toArray('.product-card').forEach((card, index) => {
        gsap.from(card, {
            scrollTrigger: {
                trigger: card,
                start: 'top 80%',
                toggleActions: 'play none none none'
            },
            duration: 0.6,
            y: 50,
            opacity: 0,
            delay: index * 0.1,
            ease: 'power2.out'
        });
    });
    
    gsap.utils.toArray('section').forEach((section, index) => {
        if (index > 0) {
            gsap.from(section, {
                scrollTrigger: {
                    trigger: section,
                    start: 'top 80%',
                    toggleActions: 'play none none none'
                },
                duration: 0.8,
                y: 30,
                opacity: 0,
                ease: 'power2.out'
            });
        }
    });
}

function initProductCardAnimations() {
    const cards = document.querySelectorAll('.product-card .card');
    cards.forEach(card => {
        card.addEventListener('mouseenter', () => {
            gsap.to(card, {
                duration: 0.3,
                scale: 1.05,
                y: -5,
                ease: 'power2.out'
            });
        });
        
        card.addEventListener('mouseleave', () => {
            gsap.to(card, {
                duration: 0.3,
                scale: 1,
                y: 0,
                ease: 'power2.out'
            });
        });
    });
}

// Re-run animations when products are loaded
const originalRenderProducts = window.renderProducts;
if (originalRenderProducts) {
    window.renderProducts = function(...args) {
        originalRenderProducts(...args);
        setTimeout(() => {
            initScrollAnimations();
            initProductCardAnimations();
        }, 100);
    };
}

